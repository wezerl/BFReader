/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.wezerl.bfreader.reddit;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.wezerl.bfreader.account.RedditAccount;
import com.wezerl.bfreader.account.RedditAccountManager;
import com.wezerl.bfreader.activities.BaseActivity;
import com.wezerl.bfreader.activities.SessionChangeListener;
import com.wezerl.bfreader.cache.CacheManager;
import com.wezerl.bfreader.cache.CacheRequest;
import com.wezerl.bfreader.cache.CacheRequestJSONParser;
import com.wezerl.bfreader.cache.downloadstrategy.DownloadStrategy;
import com.wezerl.bfreader.common.AndroidCommon;
import com.wezerl.bfreader.common.Constants;
import com.wezerl.bfreader.common.General;
import com.wezerl.bfreader.common.Optional;
import com.wezerl.bfreader.common.PrefsUtility;
import com.wezerl.bfreader.common.Priority;
import com.wezerl.bfreader.common.RRError;
import com.wezerl.bfreader.fragments.CommentListingFragment;
import com.wezerl.bfreader.http.FailedRequestBody;
import com.wezerl.bfreader.jsonwrap.JsonArray;
import com.wezerl.bfreader.jsonwrap.JsonObject;
import com.wezerl.bfreader.jsonwrap.JsonValue;
import com.wezerl.bfreader.reddit.prepared.RedditChangeDataManager;
import com.wezerl.bfreader.reddit.prepared.RedditParsedComment;
import com.wezerl.bfreader.reddit.prepared.RedditParsedPost;
import com.wezerl.bfreader.reddit.prepared.RedditPreparedPost;
import com.wezerl.bfreader.reddit.prepared.RedditRenderableComment;
import com.wezerl.bfreader.reddit.things.RedditComment;
import com.wezerl.bfreader.reddit.things.RedditPost;
import com.wezerl.bfreader.reddit.things.RedditThing;
import com.wezerl.bfreader.reddit.url.RedditURLParser;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

public class CommentListingRequest {

	private final Context mContext;
	private final CommentListingFragment mFragment;
	private final BaseActivity mActivity;
	private final RedditURLParser.RedditURL mCommentListingURL;

	private final boolean mParsePostSelfText;
	private final CacheManager mCacheManager;
	private final RedditURLParser.RedditURL mUrl;
	private final RedditAccount mUser;
	private final UUID mSession;
	private final DownloadStrategy mDownloadStrategy;

	private final Listener mListener;

	public CommentListingRequest(
			final Context context,
			final CommentListingFragment fragment,
			final BaseActivity activity,
			final RedditURLParser.RedditURL commentListingURL,
			final boolean parsePostSelfText,
			final RedditURLParser.RedditURL url,
			final RedditAccount user,
			final UUID session,
			final DownloadStrategy downloadStrategy,
			final Listener listener) {

		mContext = context;
		mFragment = fragment;
		mActivity = activity;
		mCommentListingURL = commentListingURL;
		mParsePostSelfText = parsePostSelfText;
		mUrl = url;
		mUser = user;
		mSession = session;
		mDownloadStrategy = downloadStrategy;
		mListener = listener;

		mCacheManager = CacheManager.getInstance(context);

		mCacheManager.makeRequest(createCommentListingCacheRequest());
	}

	@UiThread
	public interface Listener {

		void onCommentListingRequestDownloadNecessary();

		void onCommentListingRequestFailure(RRError error);

		void onCommentListingRequestCachedCopy(long timestamp);

		void onCommentListingRequestParseStart();

		void onCommentListingRequestPostDownloaded(RedditPreparedPost post);

		void onCommentListingRequestAllItemsDownloaded(ArrayList<RedditCommentListItem> items);
	}

	@NonNull
	private CacheRequest createCommentListingCacheRequest() {

		final URI url = General.uriFromString(mUrl.generateJsonUri().toString());

		return new CacheRequest(
				url,
				mUser,
				mSession,
				new Priority(Constants.Priority.API_COMMENT_LIST),
				mDownloadStrategy,
				Constants.FileType.COMMENT_LIST,
				CacheRequest.DOWNLOAD_QUEUE_REDDIT_API,
				mContext,
				new CacheRequestJSONParser(mContext, new CacheRequestJSONParser.Listener() {
					@Override
					public void onJsonParsed(
							@NonNull final JsonValue value,
							final long timestamp,
							@NonNull final UUID session,
							final boolean fromCache) {

						String parentPostAuthor = null;

						if(mActivity instanceof SessionChangeListener) {
							((SessionChangeListener)mActivity).onSessionChanged(
									session,
									SessionChangeListener.SessionChangeType.COMMENTS,
									timestamp);
						}

						final Integer minimumCommentScore
								= PrefsUtility.pref_behaviour_comment_min();

						if(fromCache) {
							AndroidCommon.runOnUiThread(()
									-> mListener.onCommentListingRequestCachedCopy(timestamp));
						}

						AndroidCommon.runOnUiThread(mListener::onCommentListingRequestParseStart);

						try {
							// Download main post
							if(value.asArray() != null) {

								// lol, reddit api
								final JsonArray root = value.asArray();
								final JsonObject thing = root.get(0).asObject();
								final JsonObject listing = thing.getObject("data");
								final JsonArray postContainer
										= listing.getArray("children");
								final RedditThing postThing =
										postContainer.getObject(0, RedditThing.class);
								final RedditPost post = postThing.asPost();

								final RedditParsedPost parsedPost =
										new RedditParsedPost(mActivity, post, mParsePostSelfText);

								final RedditPreparedPost preparedPost = new RedditPreparedPost(
										mContext,
										mCacheManager,
										0,
										parsedPost,
										timestamp,
										true,
										false,
										false,
										false);

								AndroidCommon.runOnUiThread(()
										-> mListener.onCommentListingRequestPostDownloaded(
												preparedPost));

								parentPostAuthor = parsedPost.getAuthor();
							}

							// Download comments

							final JsonObject thing;

							if(value.asArray() != null) {
								thing = value.asArray().get(1).asObject();
							} else {
								thing = value.asObject();
							}

							final JsonObject listing = thing.getObject("data");
							final JsonArray topLevelComments
									= listing.getArray("children");

							final ArrayList<RedditCommentListItem> items
									= new ArrayList<>(200);

							for(final JsonValue commentThingValue : topLevelComments) {
								buildCommentTree(
										commentThingValue,
										null,
										items,
										minimumCommentScore,
										parentPostAuthor);
							}

							final RedditChangeDataManager changeDataManager
									= RedditChangeDataManager.getInstance(mUser);

							for(final RedditCommentListItem item : items) {
								if(item.isComment()) {
									changeDataManager.update(
											timestamp,
											item.asComment().getParsedComment().getRawComment());
								}
							}

							AndroidCommon.runOnUiThread(()
									-> mListener.onCommentListingRequestAllItemsDownloaded(items));

						} catch(final Throwable t) {
							onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"Parse failure",
									Optional.of(new FailedRequestBody(value)));
						}
					}

					@Override
					public void onFailure(
							final int type,
							@Nullable final Throwable t,
							@Nullable final Integer httpStatus,
							@Nullable final String readableMessage,
							@NonNull final Optional<FailedRequestBody> body) {

						final RRError error = General.getGeneralErrorForFailure(
								mContext,
								type,
								t,
								httpStatus,
								url == null ? null : url.toString(),
								body);

						AndroidCommon.runOnUiThread(()
								-> mListener.onCommentListingRequestFailure(error));
					}

					@Override
					public void onDownloadNecessary() {
						AndroidCommon.runOnUiThread(
								mListener::onCommentListingRequestDownloadNecessary);
					}
				}));
	}

	private void buildCommentTree(
			final JsonValue value,
			final RedditCommentListItem parent,
			final ArrayList<RedditCommentListItem> output,
			final Integer minimumCommentScore,
			final String parentPostAuthor) throws
					IllegalAccessException,
					InstantiationException,
					NoSuchMethodException,
					InvocationTargetException {

		final RedditThing thing = value.asObject(RedditThing.class);

		if(thing.getKind() == RedditThing.Kind.MORE_COMMENTS
				&& mUrl.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL) {

			output.add(new RedditCommentListItem(
					thing.asMoreComments(),
					parent,
					mFragment,
					mActivity,
					mCommentListingURL));

		} else if(thing.getKind() == RedditThing.Kind.COMMENT) {

			final RedditComment comment = thing.asComment();
			final String currentCanonicalUserName = RedditAccountManager.getInstance(mContext)
					.getDefaultAccount().getCanonicalUsername();
			final boolean showSubredditName = !(mCommentListingURL != null
					&& mCommentListingURL.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL);
			final boolean neverAutoCollapse = mCommentListingURL != null
					&& mCommentListingURL.pathType() == RedditURLParser.USER_COMMENT_LISTING_URL;

			final RedditCommentListItem item = new RedditCommentListItem(
					new RedditRenderableComment(
							new RedditParsedComment(comment, mActivity),
							parentPostAuthor,
							minimumCommentScore,
							currentCanonicalUserName,
							true,
							showSubredditName,
							neverAutoCollapse),
					parent,
					mFragment,
					mActivity,
					mCommentListingURL);

			output.add(item);

			if(comment.replies.asObject() != null) {

				final JsonObject replies = comment.replies.asObject();
				final JsonArray children =
						replies.getObject("data").getArray("children");

				for(final JsonValue v : children) {
					buildCommentTree(
							v,
							item,
							output,
							minimumCommentScore,
							parentPostAuthor);
				}
			}
		}
	}
}
