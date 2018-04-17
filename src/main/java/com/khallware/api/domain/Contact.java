// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.khallware.api.ContactFactory;
import com.khallware.api.APIException;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ezvcard.property.Uid;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import java.util.Date;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.xml.sax.SAXException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Contacts.  A Contact is a person of interest.
 *
 * @author khall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = Contact.TABLE)
public class Contact extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Contact.class);
	public static final String TABLE = "contacts";
	public static final String COL_ORG = "organization";
	public static final String COL_ID = "id";

	public static final class Builder
			extends APIEntity.Builder<Builder, Contact>
	{
		public Builder(Contact contact)
		{
			super(contact);
			entity.modified = new Date();
		}

		public Builder uid(String uid)
		{
			entity.uid = uid;
			return(this);
		}

		public Builder org(String org)
		{
			entity.org = org;
			return(this);
		}

		public Builder name(String name)
		{
			entity.name = name;
			return(this);
		}

		public Builder title(String title)
		{
			entity.title = title;
			return(this);
		}

		public Builder email(String email)
		{
			entity.email = email;
			return(this);
		}

		public Builder phone(String phone)
		{
			entity.phone = phone;
			return(this);
		}

		public Builder vcard(String vcard)
		{
			entity.vcard = vcard;
			return(this);
		}

		public Builder address(String address)
		{
			entity.address = address;
			return(this);
		}

		public Builder description(String description)
		{
			entity.description = description;
			return(this);
		}

		public Contact build()
		{
			return(entity);
		}
	}

	@DatabaseField private String name = null;
	@DatabaseField private String email = null;
	@DatabaseField(columnName = COL_ORG) private String org = null;
	@DatabaseField private String phone = null;
	@DatabaseField private String uid = null;
	@DatabaseField private String title = null;
	@DatabaseField private String address = null;
	@DatabaseField private String vcard = null;
	@DatabaseField private String description = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Contact() {}

	@Override
	public String getFileExtension()
	{
		return("vcd");
	}

	@Override
	public void preSave()
	{
		try {
			VCard vcard = null;
			super.preSave();

			if ((vcard = parse(getVcard())) != null) {
				updateContact(this, vcard);
			}
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	public static Builder builder()
	{
		return(new Builder(new Contact()));
	}

	public String getTitle()
	{
		return(title);
	}

	public static VCard parse(String text)
	{
		VCard retval = Ezvcard.parse(text).first();
		return(retval);
	}

	public static VCard parseJson(String json)
	{
		VCard retval = Ezvcard.parseJson(json).first();
		return(retval);
	}

	public static VCard parseXml(String xml) throws APIException
	{
		VCard retval = null;
		try {
			retval = Ezvcard.parseXml(xml).first();
		}
		catch (SAXException e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
			logger.trace("XML (part): \"{}\"",
				xml.substring(0, Math.min(xml.length(), 25)));
		}
		return(retval);
	}

	public static String vcardToString(VCard vcard)
	{
		return(Ezvcard.writeXml(vcard).go());
	}

	public static String vcardToJson(VCard vcard)
	{
		return(Ezvcard.writeJson(vcard).go());
	}

	public static String toJcard(Contact contact)
	{
		StringBuilder retval = new StringBuilder();
		String token = null;

		// see: RFC-7095
		retval.append("[\"vcard\",");
		retval.append(   "[");
		retval.append(     "[\"version\",{},\"text\",\"4.0\"],");

		if (!isNullOrEmpty((token = contact.getUID()))) {
			retval.append("[\"uid\",{},\"text\",\""+token+"\"],");
		}
		if (!isNullOrEmpty((token = contact.getName()))) {
			retval.append("[\"fn\",{},\"text\",\""+token+"\"],");
		}
		if (!isNullOrEmpty((token = contact.getEmail()))) {
			retval.append("[\"email\",{\"type\":\"work\"},");
			retval.append(   "\"text\",\""+token+"\"],");
		}
		if (!isNullOrEmpty((token = contact.getPhone()))) {
			retval.append("[\"tel\",{\"type\":\"work\"},");
			retval.append(   "\"uri\",\"tel:+1-"+token+"\"],");
		}
		if (!isNullOrEmpty((token = contact.getTitle()))) {
			retval.append("[\"title\",{},\"text\",\""+token+"\"],");
		}
		if (!isNullOrEmpty((token = contact.getOrganization()))) {
			retval.append("[\"org\",{\"type\":\"work\"},");
			retval.append(   "\"text\",\""+token+"\"],");
		}
		if (!isNullOrEmpty((token = contact.getAddress()))) {
			retval.append("[\"adr\",{\"type\":\"work\"},");
			retval.append(   "\"text\",\"[");

			for (String ln : token.split("\n")) {
				retval.append("\""+ln+"\",");
			}
			retval.append(   "]],");
		}
		retval.append("[\"rev\",{},\"timestamp\","
			+"\""+LocalDateTime.now().format(
				DateTimeFormatter.ISO_INSTANT)+"\"]");
		retval.append(   "]");
		retval.append("]");
		return(""+retval);
	}

	/**
	 * Contact is not a lossless datastructure.  Pull from the vcard,
	 * but do not update it.  It's okay to update the contact, though.
	 * Update the vcard in Contact as a whole if the UID matches and
	 * the date is newer.
	 */
	public static Contact updateContact(Contact c1, VCard vcard)
			throws APIException
	{
		Contact c2 = ContactFactory.make(vcard);
		boolean replace = (!c1.getUID().equals(c2.getUID()));
		replace |= (c1.getUID().equals(c2.getUID())
			&& c1.getModified().before(c2.getModified()));
		return(updateContact(replace, c1, vcard));
	}

	protected static boolean isNullOrEmpty(String val)
	{
		return((val == null) || val.isEmpty());
	}

	public static Contact updateContact(boolean replace, Contact retval,
			VCard vcard) throws APIException
	{
		Contact contact = ContactFactory.make(vcard);

		if (replace || isNullOrEmpty(retval.getUID())) {
			if (isNullOrEmpty(contact.getUID())) {
				contact.setUID(""+UUID.randomUUID());
			}
			retval.setUID(contact.getUID());
		}
		if (replace || isNullOrEmpty(retval.getName())) {
			retval.setName(contact.getName());
		}
		if (replace || isNullOrEmpty(retval.getEmail())) {
			retval.setEmail(contact.getEmail());
		}
		if (replace || isNullOrEmpty(retval.getPhone())) {
			retval.setPhone(contact.getPhone());
		}
		if (replace || isNullOrEmpty(retval.getAddress())) {
			retval.setAddress(contact.getAddress());
		}
		if (replace || isNullOrEmpty(retval.getOrganization())) {
			retval.setOrganization(contact.getOrganization());
		}
		if (replace || isNullOrEmpty(retval.getVcard())) {
			retval.setVcard(contact.getVcard());
		}
		return(retval);
	}

	public String getUID()
	{
		return((uid == null) ? "" : uid);
	}

	public void setUID(String uid)
	{
		if (uid != null && !uid.isEmpty()) {
			try {
				this.uid = ""+UUID.fromString(uid);
			}
			catch (Exception e1) {
				try {
					this.uid = ""+new Uid(uid).getValue();
				}
				catch (Exception e2) {
					logger.warn("uid convention not right: "
						+e1+", "+e2);
					logger.trace(""+e1, e1);
					logger.trace(""+e2, e2);
					this.uid = uid;
				}
			}
		}
	}

	public String getName()
	{
		return(name);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getVcard()
	{
		return((vcard == null) ? "" : vcard);
	}

	public void setVcard(String vcard)
	{
		this.vcard = vcard;
	}

	public String getDescription()
	{
		return(description);
	}

	public String getEmail()
	{
		return(email);
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPhone()
	{
		return(phone);
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getAddress()
	{
		return(address);
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getOrganization()
	{
		return(org);
	}

	public void setOrganization(String org)
	{
		this.org = org;
	}

	@Override
	public int hashCode()
	{
		return(getId());
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean retval = true;
		retval &= (obj != null);
		retval &= (retval && obj.getClass() == this.getClass());

		if (this.hashCode() != UNKNOWN && obj.hashCode() != UNKNOWN) {
			retval &= (this.hashCode() == obj.hashCode());
		}
		else {
			retval &= (this.getUID().equals(
				((Contact)obj).getUID()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("uid=\""+getUID()+"\", ")
			.append("name=\""+getName()+"\", ")
			.append("email=\""+getEmail()+"\", ")
			.append("phone=\""+getPhone()+"\", ")
			.append("address=\""+getAddress()+"\", ")
			.append("vcard_sz="+getVcard().length()+" bytes, ")
			.append("org=\""+getOrganization()+"\"")
			.toString());
	}
}
