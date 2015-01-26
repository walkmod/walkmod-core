/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/

package org.walkmod.exceptions;

import org.walkmod.util.location.Location;
import org.walkmod.util.location.LocationImpl;
import org.walkmod.util.location.LocationUtils;

public class WalkModException extends RuntimeException {

    /**
	 * serialVersionUID
	 */
    private static final long serialVersionUID = 6023420721454117372L;

    private Location location;

    public WalkModException() {
    }

    /**
	 * Constructs a <code>XWorkException</code> with the specified detail
	 * message.
	 * 
	 * @param s
	 *            the detail message.
	 */
    public WalkModException(String s) {
        this(s, null, null);
    }

    /**
	 * Constructs a <code>XWorkException</code> with the specified detail
	 * message and target.
	 * 
	 * @param s
	 *            the detail message.
	 * @param target
	 *            the target of the exception.
	 */
    public WalkModException(String s, Object target) {
        this(s, (Throwable) null, target);
    }

    /**
	 * Constructs a <code>XWorkException</code> with the root cause
	 * 
	 * @param cause
	 *            The wrapped exception
	 */
    public WalkModException(Throwable cause) {
        this(null, cause, null);
        if (cause != null) {
            this.setStackTrace(cause.getStackTrace());
        }
    }

    /**
	 * Constructs a <code>XWorkException</code> with the root cause and target
	 * 
	 * @param cause
	 *            The wrapped exception
	 * @param target
	 *            The target of the exception
	 */
    public WalkModException(Throwable cause, Object target) {
        this(null, cause, target);
        if (cause != null) {
            this.setStackTrace(cause.getStackTrace());
        }
    }

    /**
	 * Constructs a <code>XWorkException</code> with the specified detail
	 * message and exception cause.
	 * 
	 * @param s
	 *            the detail message.
	 * @param cause
	 *            the wrapped exception
	 */
    public WalkModException(String s, Throwable cause) {
        this(s, cause, null);
        if (cause != null) {
            this.setStackTrace(cause.getStackTrace());
        }
    }

    /**
	 * Constructs a <code>XWorkException</code> with the specified detail
	 * message, cause, and target
	 * 
	 * @param s
	 *            the detail message.
	 * @param cause
	 *            The wrapped exception
	 * @param target
	 *            The target of the exception
	 */
    public WalkModException(String s, Throwable cause, Object target) {
        super(s, cause);
        this.location = LocationUtils.getLocation(target);
        if (this.location == LocationImpl.UNKNOWN) {
            this.location = LocationUtils.getLocation(cause);
        }
        if (cause != null) {
            this.setStackTrace(cause.getStackTrace());
        }
    }

    /**
	 * Gets the underlying cause
	 * 
	 * @return the underlying cause, <tt>null</tt> if no cause
	 * @deprecated Use {@link #getCause()}
	 */
    @Deprecated
    public Throwable getThrowable() {
        return getCause();
    }

    /**
	 * Gets the location of the error, if available
	 * 
	 * @return the location, <tt>null</tt> if not available
	 */
    public Location getLocation() {
        return this.location;
    }

    /**
	 * Returns a short description of this throwable object, including the
	 * location. If no detailed message is available, it will use the message of
	 * the underlying exception if available.
	 * 
	 * @return a string representation of this <code>Throwable</code>.
	 */
    @Override
    public String toString() {
        String msg = getMessage();
        if (msg == null && getCause() != null) {
            msg = getCause().getMessage();
        }
        if (location != null) {
            if (msg != null) {
                return msg + " - " + location.toString();
            } else {
                return location.toString();
            }
        } else {
            return msg;
        }
    }
}
