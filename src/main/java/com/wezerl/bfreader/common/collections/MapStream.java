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

public class MapStream<Input, Output> extends Stream<Output> {

	public interface Operator<Input, Output> {
		Output operate(Input value);
	}

	private final Stream<Input> mInput;
	private final Operator<Input, Output> mOperator;

	public MapStream(
			final Stream<Input> input,
			final Operator<Input, Output> operator) {

		mInput = input;
		mOperator = operator;
	}

	@Override
	public boolean hasNext() {
		return mInput.hasNext();
	}

	@Override
	public Output take() {
		return mOperator.operate(mInput.take());
	}
}
