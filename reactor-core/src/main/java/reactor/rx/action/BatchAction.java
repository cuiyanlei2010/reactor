/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.rx.action;

import reactor.event.dispatch.Dispatcher;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Stephane Maldini
 * @since 1.1
 */
public abstract class BatchAction<T, V> extends Action<T, V> {

	final boolean next;
	final boolean flush;
	final boolean first;

	private long errorCount  = 0l;
	private long acceptCount = 0l;

	public BatchAction(int batchSize,
	                   Dispatcher dispatcher, boolean next, boolean first, boolean flush) {
		super(dispatcher, batchSize);
		this.first = first;
		this.flush = flush;
		this.next = next;
	}

	protected void nextCallback(T event) {
	}

	protected void flushCallback(T event) {
	}

	protected void firstCallback(T event) {
	}

	@Override
	protected void doNext(T value) {
		long accepted;
		accepted = (++acceptCount) % batchSize;

		if (first && accepted == 1) {
			firstCallback(value);
		}

		if (next) {
			nextCallback(value);
		}

		if (flush && accepted == 0) {
			flushCallback(value);
		}
	}

	@Override
	protected void doError(Throwable error) {
		errorCount++;
		super.doError(error);
	}

	@Override
	protected void doComplete() {
		flushCallback(null);
		super.doComplete();
	}

	@Override
	public void available() {
		flushCallback(null);
		super.available();
	}

	@Override
	protected void requestUpstream(AtomicLong capacity, boolean terminated, int elements) {
		flushCallback(null);
		if (capacity.get() > 0) {
			super.requestUpstream(capacity, terminated, elements);
		}
	}

	@Override
	public String toString() {
		return super.toString() + "{accepted=" + acceptCount + ", errors=" + errorCount + "}";
	}
}
