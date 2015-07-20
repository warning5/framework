package com.hwtxframework.ioc;
/**
 * 
 * @author chao cai
 *  the client implemented this interface, will be noticed when service is ready
 */
public interface ComponentChangeListener {
	/**
	 * The method is invoked when some service is ready
	 * @param bundle the component is ready
	 */
	public void onComponentReady(ComponentBundle bundle );
}
