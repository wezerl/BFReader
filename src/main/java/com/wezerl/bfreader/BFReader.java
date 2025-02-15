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

package com.wezerl.bfreader;

import android.app.Application;
import android.util.Log;
import com.wezerl.bfreader.cache.CacheManager;
import com.wezerl.bfreader.common.Alarms;
import com.wezerl.bfreader.common.Fonts;
import com.wezerl.bfreader.common.GlobalExceptionHandler;
import com.wezerl.bfreader.common.PrefsUtility;
import com.wezerl.bfreader.io.RedditChangeDataIO;
import com.wezerl.bfreader.receivers.NewMessageChecker;
import com.wezerl.bfreader.receivers.announcements.AnnouncementDownloader;
import com.wezerl.bfreader.reddit.prepared.RedditChangeDataManager;

public class BFReader extends Application {

	@Override
	public void onCreate() {

		super.onCreate();

		Log.i("RedReader", "Application created.");

		GlobalExceptionHandler.init(this);

		PrefsUtility.init(this);

		Fonts.onAppCreate(getAssets());

		final CacheManager cm = CacheManager.getInstance(this);

		new Thread() {
			@Override
			public void run() {

				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

				cm.pruneTemp();
				cm.pruneCache();
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				RedditChangeDataIO.getInstance(com.wezerl.bfreader.BFReader.this)
						.runInitialReadInThisThread();
				RedditChangeDataManager.pruneAllUsersDefaultMaxAge();
			}
		}.start();

		Alarms.onBoot(this);

		AnnouncementDownloader.performDownload(this);
		NewMessageChecker.checkForNewMessages(this);
	}
}
