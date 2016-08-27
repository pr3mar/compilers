package compiler.data.frg;

import compiler.common.logger.Logger;

/**
 * A fragment containing a variable.
 * 
 * @author sliva
 */
public class DataFragmentMem extends DataFragment {

	/** The size of a variable. */
	public final long memoryLocation;

	/**
	 * Constructs a new fragment containing a variable.
	 *
	 * @param label
	 *            The label of this fragment.
	 * @param width
	 *            The size of a variable.
	 */
	public DataFragmentMem(String label, long width, long memoryLocation) {
		super(label, width);
		this.memoryLocation = memoryLocation;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("frg");
		logger.addAttribute("kind", "DATA " + "(" + label + "," + width + "," + memoryLocation + ")");
		logger.endElement();
	}

}
