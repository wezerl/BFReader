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

package com.wezerl.bfreader.reddit.api;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wezerl.bfreader.account.RedditAccount;
import com.wezerl.bfreader.common.TimestampBound;
import com.wezerl.bfreader.common.collections.WeakReferenceListManager;
import com.wezerl.bfreader.io.RawObjectDB;
import com.wezerl.bfreader.io.RequestResponseHandler;
import com.wezerl.bfreader.io.WritableHashSet;

import java.util.ArrayList;
import java.util.HashSet;

public class RedditMultiredditSubscriptionManager {

	private final MultiredditListChangeNotifier notifier
			= new MultiredditListChangeNotifier();
	private final WeakReferenceListManager<MultiredditListChangeListener> listeners
			= new WeakReferenceListManager<>();

	@SuppressLint("StaticFieldLeak") private static RedditMultiredditSubscriptionManager singleton;
	private static RedditAccount singletonAccount;

	@NonNull private final RedditAccount mUser;
	@NonNull private final Context mContext;

	private static RawObjectDB<String, WritableHashSet> db = null;

	private WritableHashSet mMultireddits;

	public static synchronized RedditMultiredditSubscriptionManager getSingleton(
			@NonNull final Context context,
			@NonNull final RedditAccount account) {

		if(db == null) {
			db = new RawObjectDB<>(
					context.getApplicationContext(),
					"rr_multireddit_subscriptions.db",
					WritableHashSet.class);
		}

		if(singleton == null
				|| !account.equals(RedditMultiredditSubscriptionManager.singletonAccount)) {

			singleton = new RedditMultiredditSubscriptionManager(
					account,
					context.getApplicationContext());

			RedditMultiredditSubscriptionManager.singletonAccount = account;
		}

		return singleton;
	}

	private RedditMultiredditSubscriptionManager(
			@NonNull final RedditAccount user,
			@NonNull final Context context) {

		this.mUser = user;
		this.mContext = context;

		mMultireddits = db.getById(user.getCanonicalUsername());
	}

	public void addListener(@NonNull final MultiredditListChangeListener listener) {
		listeners.add(listener);
	}

	public synchronized boolean areSubscriptionsReady() {
		return mMultireddits != null;
	}

	private synchronized void onNewSubscriptionListReceived(
			final HashSet<String> newSubscriptions,
			final long timestamp) {

		mMultireddits = new WritableHashSet(
				newSubscriptions,
				timestamp,
				mUser.getCanonicalUsername());

		listeners.map(notifier);

		// TODO threaded? or already threaded due to cache manager
		db.put(mMultireddits);
	}

	public synchronized ArrayList<String> getSubscriptionList() {
		return new ArrayList<>(mMultireddits.toHashset());
	}

	public void triggerUpdate(
			@Nullable final RequestResponseHandler<
					HashSet<String>,
					SubredditRequestFailure> handler,
			@NonNull final TimestampBound timestampBound) {

		if(mMultireddits != null
				&& timestampBound.verifyTimestamp(mMultireddits.getTimestamp())) {
			return;
		}

		new RedditAPIMultiredditListRequester(mContext, mUser).performRequest(
				RedditAPIMultiredditListRequester.Key.INSTANCE,
				timestampBound,
				new RequestResponseHandler<WritableHashSet, SubredditRequestFailure>() {

					// TODO handle failed requests properly -- retry? then notify listeners
					@Override
					public void onRequestFailed(final SubredditRequestFailure failureReason) {
						if(handler != null) {
							handler.onRequestFailed(failureReason);
						}
					}

					@Override
					public void onRequestSuccess(
							final WritableHashSet result,
							final long timeCached) {
						final HashSet<String> newSubscriptions = result.toHashset();
						onNewSubscriptionListReceived(newSubscriptions, timeCached);
						if(handler != null) {
							handler.onRequestSuccess(newSubscriptions, timeCached);
						}
					}
				}
		);
	}

	public Long getSubscriptionListTimestamp() {
		return mMultireddits != null ? mMultireddits.getTimestamp() : null;
	}

	public interface MultiredditListChangeListener {
		void onMultiredditListUpdated(
				RedditMultiredditSubscriptionManager multiredditSubscriptionManager);
	}

	private class MultiredditListChangeNotifier
			implements WeakReferenceListManager.Operator<MultiredditListChangeListener> {

		@Override
		public void operate(final MultiredditListChangeListener listener) {
			listener.onMultiredditListUpdated(
					RedditMultiredditSubscriptionManager.this);
		}
	}
}
