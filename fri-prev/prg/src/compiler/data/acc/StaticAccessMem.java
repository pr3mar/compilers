package compiler.data.acc;

import compiler.common.logger.Logger;

import java.util.Stack;

/**
 * Access to a static variable.
 * 
 * @author sliva
 */
public class StaticAccessMem extends StaticAccess {

	/**
	 * The label the variable is stored at.
	 */
	public final long memoryLocation;

	/**
	 * Constructs a new access to a static variable.
	 *
	 * @param label
	 *            The label the variable is stored at
	 * @param size
	 *            The size of a variable.
	 */
	public StaticAccessMem(String label, long size, String memoryLocation) {
		super(label, size);
		this.memoryLocation = Long.parseLong(memoryLocation.substring(1));
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("access");
		logger.addAttribute("label", label);
		logger.addAttribute("size", Long.toString(size));
		logger.addAttribute("location", Long.toString(memoryLocation));
		logger.endElement();
	}

}
