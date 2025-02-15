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

import android.content.Context;
import androidx.annotation.NonNull;
import com.wezerl.bfreader.cache.CacheRequest;
import com.wezerl.bfreader.common.General;
import com.wezerl.bfreader.common.Optional;
import com.wezerl.bfreader.common.RRError;
import com.wezerl.bfreader.http.FailedRequestBody;

import java.net.URI;

public class SubredditRequestFailure {
	public final @CacheRequest.RequestFailureType int requestFailureType;
	public final Throwable t;
	public final Integer statusLine;
	public final String readableMessage;
	public final String url;
	@NonNull public final Optional<FailedRequestBody> body;

	public SubredditRequestFailure(
			@CacheRequest.RequestFailureType final int requestFailureType,
			final Throwable t,
			final Integer statusLine,
			final String readableMessage,
			final String url,
			@NonNull final Optional<FailedRequestBody> body) {
		this.requestFailureType = requestFailureType;
		this.t = t;
		this.statusLine = statusLine;
		this.readableMessage = readableMessage;
		this.url = url;
		this.body = body;
	}

	public SubredditRequestFailure(
			@CacheRequest.RequestFailureType final int requestFailureType,
			final Throwable t,
			final Integer statusLine,
			final String readableMessage,
			final URI url,
			@NonNull final Optional<FailedRequestBody> body) {
		this(
				requestFailureType,
				t,
				statusLine,
				readableMessage,
				url != null ? url.toString() : null,
				body);
	}

	public RRError asError(final Context context) {
		return General.getGeneralErrorForFailure(
				context,
				requestFailureType,
				t,
				statusLine,
				url,
				body);
	}
}
