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

package com.wezerl.bfreader.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wezerl.bfreader.R;
import com.wezerl.bfreader.account.RedditAccountManager;
import com.wezerl.bfreader.activities.BaseActivity;
import com.wezerl.bfreader.activities.BugReportActivity;
import com.wezerl.bfreader.activities.PMSendActivity;
import com.wezerl.bfreader.cache.CacheManager;
import com.wezerl.bfreader.cache.CacheRequest;
import com.wezerl.bfreader.cache.downloadstrategy.DownloadStrategyAlways;
import com.wezerl.bfreader.common.AndroidCommon;
import com.wezerl.bfreader.common.Constants;
import com.wezerl.bfreader.common.General;
import com.wezerl.bfreader.common.LinkHandler;
import com.wezerl.bfreader.common.Optional;
import com.wezerl.bfreader.common.RRError;
import com.wezerl.bfreader.common.RRTime;
import com.wezerl.bfreader.http.FailedRequestBody;
import com.wezerl.bfreader.reddit.APIResponseHandler;
import com.wezerl.bfreader.reddit.RedditAPI;
import com.wezerl.bfreader.reddit.things.RedditUser;
import com.wezerl.bfreader.reddit.url.UserPostListingURL;
import com.wezerl.bfreader.views.liststatus.ErrorView;
import com.wezerl.bfreader.views.liststatus.LoadingView;

public class UserProfileDialog extends PropertiesDialog {

	private String username;
	private boolean active = true;

	public static UserProfileDialog newInstance(final String user) {

		final UserProfileDialog dialog = new UserProfileDialog();

		final Bundle args = new Bundle();
		args.putString("user", user);
		dialog.setArguments(args);

		return dialog;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		active = false;
	}

	@Override
	protected String getTitle(final Context context) {
		return username;
	}

	@Override
	protected void prepare(
			@NonNull final BaseActivity context,
			@NonNull final LinearLayout items) {

		final LoadingView loadingView = new LoadingView(
				context,
				R.string.download_waiting,
				true,
				true);
		items.addView(loadingView);

		username = getArguments().getString("user");
		final CacheManager cm = CacheManager.getInstance(context);

		RedditAPI.getUser(
				cm,
				username,
				new APIResponseHandler.UserResponseHandler(context) {
					@Override
					protected void onDownloadStarted() {
						if(!active) {
							return;
						}
						loadingView.setIndeterminate(R.string.download_connecting);
					}

					@Override
					protected void onSuccess(final RedditUser user, final long timestamp) {

						AndroidCommon.UI_THREAD_HANDLER.post(() -> {

							if(!active) {
								return;
							}

							loadingView.setDone(R.string.download_done);

							final LinearLayout karmaLayout
									= (LinearLayout)context.getLayoutInflater()
									.inflate(R.layout.karma, null);
							items.addView(karmaLayout);

							final LinearLayout linkKarmaLayout
									= karmaLayout.findViewById(R.id.layout_karma_link);
							final LinearLayout commentKarmaLayout
									= karmaLayout.findViewById(R.id.layout_karma_comment);
							final TextView linkKarma
									= karmaLayout.findViewById(R.id.layout_karma_text_link);
							final TextView commentKarma
									= karmaLayout.findViewById(R.id.layout_karma_text_comment);

							linkKarma.setText(String.valueOf(user.link_karma));
							commentKarma.setText(String.valueOf(user.comment_karma));

							linkKarmaLayout.setOnLongClickListener(v -> {
								final ClipboardManager clipboardManager
										= (ClipboardManager)context.getSystemService(
										Context.CLIPBOARD_SERVICE);
								if(clipboardManager != null) {
									final ClipData data = ClipData.newPlainText(
											context.getString(R.string.karma_link),
											linkKarma.getText());
									clipboardManager.setPrimaryClip(data);

									General.quickToast(
											context,
											R.string.copied_to_clipboard);
								}
								return true;
							});
							commentKarmaLayout.setOnLongClickListener(v -> {
								final ClipboardManager clipboardManager
										= (ClipboardManager)context.getSystemService(
										Context.CLIPBOARD_SERVICE);
								if(clipboardManager != null) {
									final ClipData data = ClipData.newPlainText(
											context.getString(R.string.karma_comment),
											commentKarma.getText());
									clipboardManager.setPrimaryClip(data);

									General.quickToast(
											context,
											R.string.copied_to_clipboard);
								}
								return true;
							});

							items.addView(propView(
									context,
									R.string.userprofile_created,
									RRTime.formatDateTime(
											user.created_utc * 1000,
											context),
									false));
							items.getChildAt(items.getChildCount() - 1)
									.setNextFocusUpId(R.id.layout_karma_link);

							if(user.is_friend) {
								items.addView(propView(
										context,
										R.string.userprofile_isfriend,
										R.string.general_true,
										false));
							}

							if(user.is_gold) {
								items.addView(propView(
										context,
										R.string.userprofile_isgold,
										R.string.general_true,
										false));
							}

							if(user.is_mod) {
								items.addView(propView(
										context,
										R.string.userprofile_moderator,
										R.string.general_true,
										false));
							}

							final Button commentsButton = new Button(context);
							commentsButton.setText(R.string.userprofile_viewcomments);
							commentsButton.setOnClickListener(v -> LinkHandler.onLinkClicked(
									context,
									Constants.Reddit.getUri("/user/"
											+ username
											+ "/comments.json")
											.toString(),
									false));
							items.addView(commentsButton);
							// TODO use margin? or framelayout? scale padding dp
							// TODO change button color
							commentsButton.setPadding(20, 20, 20, 20);

							final Button postsButton = new Button(context);
							postsButton.setText(R.string.userprofile_viewposts);
							postsButton.setOnClickListener(v -> LinkHandler.onLinkClicked(
									context,
									UserPostListingURL.getSubmitted(username)
											.generateJsonUri()
											.toString(),
									false));
							items.addView(postsButton);
							// TODO use margin? or framelayout? scale padding dp
							postsButton.setPadding(20, 20, 20, 20);

							if(!RedditAccountManager.getInstance(context)
									.getDefaultAccount()
									.isAnonymous()) {
								final Button pmButton = new Button(context);
								pmButton.setText(R.string.userprofile_pm);
								pmButton.setOnClickListener(v -> {
									final Intent intent = new Intent(
											context,
											PMSendActivity.class);
									intent.putExtra(
											PMSendActivity.EXTRA_RECIPIENT,
											username);
									startActivity(intent);
								});
								items.addView(pmButton);
								pmButton.setPadding(20, 20, 20, 20);
							}
						});
					}

					@Override
					protected void onCallbackException(final Throwable t) {
						BugReportActivity.handleGlobalError(context, t);
					}

					@Override
					protected void onFailure(
							final @CacheRequest.RequestFailureType int type,
							final Throwable t,
							final Integer status,
							final String readableMessage,
							@NonNull final Optional<FailedRequestBody> response) {

						AndroidCommon.UI_THREAD_HANDLER.post(() -> {

							if(!active) {
								return;
							}

							loadingView.setDone(R.string.download_failed);

							final RRError error = General.getGeneralErrorForFailure(
									context,
									type,
									t,
									status,
									null,
									response);
							items.addView(new ErrorView(context, error));
						});
					}

					@Override
					protected void onFailure(
							@NonNull final APIFailureType type,
							@Nullable final String debuggingContext,
							@NonNull final Optional<FailedRequestBody> response) {

						AndroidCommon.UI_THREAD_HANDLER.post(() -> {

							if(!active) {
								return;
							}

							loadingView.setDone(R.string.download_failed);

							final RRError error = General.getGeneralErrorForFailure(
									context,
									type,
									debuggingContext,
									response);
							items.addView(new ErrorView(context, error));
						});
					}

				},
				RedditAccountManager.getInstance(context).getDefaultAccount(),
				DownloadStrategyAlways.INSTANCE,
				context);
	}
}
