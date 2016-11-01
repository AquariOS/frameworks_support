/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.support.lifecycle;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

/**
 * An implementation of {@link Lifecycle} that can handle multiple observers.
 * <p>
 * It is used by Fragments and Support Library Activities. You can also directly use it if you have
 * a custom LifecycleProvider.
 */
@SuppressWarnings("WeakerAccess")
public class LifecycleRegistry implements Lifecycle {
    static final String TAG = "LifecycleRegistry";
    /**
     * Custom list that keeps observers and can handle removals / additions during traversal.
     */
    private ObserverList mObserverList = new ObserverList();

    /**
     * Latest state that was provided via {@link #setCurrentState(int)}.
     */
    @State
    private int mCurrentState;

    /**
     * Previously set state. This allows us to re-use the dispatcher while traversing listeners.
     */
    @VisibleForTesting
    private int mPrevState;

    /**
     * The provider that owns this Lifecycle.
     */
    private final LifecycleProvider mLifecycleProvider;

    private final ObserverList.Callback mDispatchCallback = new ObserverList.Callback() {
        @Override
        public void run(GenericLifecycleObserver observer) {
            observer.onStateChanged(mLifecycleProvider, mPrevState);
        }
    };

    /**
     * Creates a new LifecycleRegistry for the given provider.
     * <p>
     * You should usually create this inside your LifecycleProvider class's constructor and hold
     * onto the same instance.
     *
     * @param provider     The owner LifecycleProvider
     * @param initialState The start state.
     */
    public LifecycleRegistry(@NonNull LifecycleProvider provider, @State int initialState) {
        mCurrentState = initialState;
        mLifecycleProvider = provider;
    }

    /**
     * Sets the current state and notifies the observers.
     * <p>
     * Note that if the {@code currentState} is the same state as the last call to this method,
     * calling this method has no effect.
     *
     * @param currentState The updated state of the LifecycleProvider.
     */
    public void setCurrentState(@State int currentState) {
        if (mCurrentState == currentState) {
            return;
        }
        mPrevState = mCurrentState;
        mCurrentState = currentState;
        mObserverList.forEach(mDispatchCallback);
    }

    @Override
    public void addObserver(LifecycleObserver observer) {
        mObserverList.add(observer);
    }

    @Override
    public void removeObserver(LifecycleObserver observer) {
        mObserverList.remove(observer);
    }

    /**
     * The number of observers.
     *
     * @return The number of observers.
     */
    public int size() {
        return mObserverList.size();
    }

    @Override
    @State
    public int getCurrentState() {
        return mCurrentState;
    }
}
