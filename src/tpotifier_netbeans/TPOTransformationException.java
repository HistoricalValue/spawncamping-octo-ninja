/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tpotifier_netbeans;

/**
 *
 * @author amalia
 */
public class TPOTransformationException extends RuntimeException {

    private final static long serialVersionUID = 0x930;

    /**
     * Creates a new instance of <code>TPOTransformationException</code> without detail message.
     */
    public TPOTransformationException() {
    }


    /**
     * Constructs an instance of <code>TPOTransformationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TPOTransformationException(String msg) {
        super(msg);
    }

    public TPOTransformationException(Throwable cause) {
    }

    public TPOTransformationException(String message, Throwable cause) {
    }

}
