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

package com.wezerl.bfreader.common.collections;

import java.util.Arrays;
import java.util.Iterator;

public class CollectionStream<Type> extends Stream<Type> {

	private final Iterator<Type> mIterator;

	public CollectionStream(final Iterable<Type> iterable) {
		mIterator = iterable.iterator();
	}

	@SafeVarargs
	public CollectionStream(final Type... array) {
		this(Arrays.asList(array));
	}

	@Override
	public boolean hasNext() {
		return mIterator.hasNext();
	}

	@Override
	public Type take() {
		return mIterator.next();
	}
}
