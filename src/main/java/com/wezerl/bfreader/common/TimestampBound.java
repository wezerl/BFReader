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

package com.wezerl.bfreader.common;

public abstract class TimestampBound {

	public abstract boolean verifyTimestamp(long timestamp);

	public static final TimestampBound ANY = new TimestampBound() {
		@Override
		public boolean verifyTimestamp(final long timestamp) {
			return true;
		}
	};
	public static final TimestampBound NONE = new TimestampBound() {
		@Override
		public boolean verifyTimestamp(final long timestamp) {
			return false;
		}
	};

	public static final class MoreRecentThanBound extends TimestampBound {

		private final long minTimestamp;

		public MoreRecentThanBound(final long minTimestamp) {
			this.minTimestamp = minTimestamp;
		}

		@Override
		public boolean verifyTimestamp(final long timestamp) {
			return timestamp >= minTimestamp;
		}
	}

	public static MoreRecentThanBound notOlderThan(final long ageMs) {
		return new MoreRecentThanBound(System.currentTimeMillis() - ageMs);
	}
}
