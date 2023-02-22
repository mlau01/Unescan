package unescan.gui;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

class PanelDevice {
	private final Composite container;
	private final Combo in_model, in_owner;
	private final Text in_serial, in_clientId, in_comment;
	private final Button b_valid, b_cancel;
	
	public PanelDevice(final Composite p_container, final Combo p_in_model, final Text p_in_serial, final Combo p_in_owner, final Text p_in_clientId, final Text p_in_comment, final Button p_b_valid, final Button p_b_cancel)
	{
		container = p_container;
		in_model = p_in_model;
		in_serial = p_in_serial;
		in_owner = p_in_owner;
		in_clientId = p_in_clientId;
		in_comment = p_in_comment;
		b_valid = p_b_valid;
		b_cancel = p_b_cancel;
	}

	/**
	 * @return the container
	 */
	public Composite getContainer() {
		return container;
	}

	/**
	 * @return the in_model
	 */
	public Combo getIn_model() {
		return in_model;
	}

	/**
	 * @return the in_serial
	 */
	public Text getIn_serial() {
		return in_serial;
	}

	/**
	 * @return the in_clientId
	 */
	public Text getIn_clientId() {
		return in_clientId;
	}

	/**
	 * @return the b_valid
	 */
	public Button getB_valid() {
		return b_valid;
	}

	/**
	 * @return the in_comment
	 */
	public Text getIn_comment() {
		return in_comment;
	}

	/**
	 * @return the in_owner
	 */
	public Combo getIn_owner() {
		return in_owner;
	}

	/**
	 * @return the b_cancel
	 */
	public Button getB_cancel() {
		return b_cancel;
	}

}
