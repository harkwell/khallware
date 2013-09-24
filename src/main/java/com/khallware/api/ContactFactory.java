// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import com.khallware.api.validation.CompleteContact;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Contact;
import ezvcard.property.SimpleProperty;
import ezvcard.property.VCardProperty;
import ezvcard.property.Organization;
import ezvcard.property.Telephone;
import ezvcard.property.Address;
import ezvcard.VCardVersion;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Create Contact instances or lists of Contact instances.
 * @author khall
 */
public class ContactFactory
{
	private static final Logger logger = LoggerFactory.getLogger(
		ContactFactory.class);

	/**
	 * Given a VCard, make a corresponding Contact.
	 *
	 * @param vcard - The VCard.
	 * @return Contact - corresponding Contact.
	 */
	public static Contact make(VCard vcard) throws APIException
	{
		Contact.Builder retval = Contact.builder();
		List<VCardProperty> list = new ArrayList<>();
		try {
			logger.debug("validation warnings: ({})",
				vcard.validate(VCardVersion.V3_0));
			retval.uid(resolve("", vcard.getUid()));
			retval.name(resolve("", vcard.getFormattedName()));
			list.clear();
			list.addAll(vcard.getEmails());
			retval.email(resolve("", list));
			list.clear();
			list.addAll(vcard.getTelephoneNumbers());
			retval.phone(resolve("", list));
			list.clear();
			list.addAll(vcard.getAddresses());
			retval.address(resolve("", list));
			list.clear();
			list.addAll(vcard.getOrganizations());
			retval.org(resolve("", list));
			logger.trace("created contact ("+retval+")");
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
			throw new APIException(e);
		}
		return(retval.build());
	}

	/**
	 * Given a textual VCard, make a corresponding Contact.
	 *
	 * @param text - The textual VCard.
	 * @return List<Contact> - one ore more Contacts.
	 */
	public static List<Contact> make(String text) throws APIException
	{
		List<Contact> retval = new ArrayList<>();
		CompleteContact validator = new CompleteContact();
		Contact contact = null;
		try {
			for (VCard vcard : Ezvcard.parse(text).all()) {
				if (validator.isValid((contact = make(vcard)))){
					contact.setVcard(
						Ezvcard.write(vcard).go());
					retval.add(contact);
				}
				else {
					logger.warn("invalid ({})", contact);
				}
			}
		}
		catch (RuntimeException e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
			throw new APIException(e);
		}
		return(retval);
	}

	/**
	 * A toString() implementation.
	 * Determine which is the most appropriate VCardProperty to return
	 * and then resolve the string value of that one.
	 *
	 * @param def - The default return value if none is resolved.
	 * @param list - The list of VCardProperty.
	 * @return String - The resolved value.
	 */
	public static String resolve(String def, List<VCardProperty> list)
	{
		return((list.size() > 0) ? resolve(def, list.get(0)) : def);
	}

	/**
	 * Determine which is the most appropriate VCardProperty to return
	 * and then resolve that one.
	 *
	 * @param def - The default return value if none is resolved.
	 * @param list - The list of VCardProperty.
	 * @return String - The resolved value.
	 */
	public static String resolve(String def, VCardProperty prop)
	{
		String retval = def;

		if (prop == null) {
			logger.trace("silently ignoring null property");
		}
		else if (prop instanceof SimpleProperty) {
			SimpleProperty sprop = (SimpleProperty)prop;

			if (sprop.getValue() != null) {
				if (!(""+sprop.getValue()).isEmpty()) {
					retval = ""+sprop.getValue();
				}
			}
		}
		else if (prop instanceof Telephone) {
			retval = ((Telephone)prop).getText();
		}
		else if (prop instanceof Address) {
			retval = ((Address)prop).getLabel();
		}
		else if (prop instanceof Organization) {
			retval = ""+((Organization)prop).getValues();
		}
		else {
			throw new RuntimeException("uknown type: "
				+prop.getClass().getSimpleName());
		}
		return(retval);
	}
}
