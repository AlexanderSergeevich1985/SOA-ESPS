/**The MIT License (MIT)
 Copyright (c) 2019 by AleksanderSergeevich
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.soaesps.core.component.curcuitBreaker;

public enum CircuitState {
    Closed(0),
    Open(1),
    Half_Open(2);

    private int value;

    private State state;

    CircuitState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    protected void setState(final State state) {
        this.value = state.getValue();
    }

    public CircuitState next() {
        state.changeState(this);

        return this;
    }

    static public abstract class State {
        private int value;

        State(final int value) {
            this.value = value;
        }

        abstract public void changeState(final CircuitState circuitState);

        public int getValue() {
            return value;
        }

        public void setValue(final int value) {
            this.value = value;
        }
    }

    static public class ClosedState extends State {
        ClosedState() {
            super(CircuitState.Closed.value);
        }

        public void changeState(final CircuitState circuitState) {
            circuitState.setState(new OpenState());
        }
    }

    static public class OpenState extends State {
        OpenState() {
            super(CircuitState.Open.value);
        }

        public void changeState(final CircuitState circuitState) {
            circuitState.setState(new HalfOpenState());
        }
    }

    static public class HalfOpenState extends State {
        HalfOpenState() {
            super(CircuitState.Half_Open.value);
        }

        public void changeState(final CircuitState circuitState) {
            circuitState.setState(new ClosedState());
        }
    }
}