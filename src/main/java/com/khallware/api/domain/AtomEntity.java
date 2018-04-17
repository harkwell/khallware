// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.DatastoreException;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A special entity that is used for RSS/atom feeds.
 *
 * @author khall
 */
public abstract class AtomEntity extends APIEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		AtomEntity.class);

	protected URL atomURL = null;

	public abstract String getTitle();
	public abstract String getDescription();

	public URL getAtomURL()
	{
		return(atomURL);
	}

	public String getFileExtension()
	{
		return("");
	}

	public void updateUrl(String baseUrl) throws APIException
	{
		StringBuilder sb = new StringBuilder();
		String ext = getFileExtension();
		ext = ((ext.startsWith(".") || ext.isEmpty()) ? "" : ".") + ext;
		sb.append(baseUrl);
		sb.append("/");
		sb.append(this.getClass().getSimpleName().toLowerCase());
		sb.append("s/");
		sb.append(getId());
		sb.append(ext);
		try {
			atomURL = new URL(""+sb);
		}
		catch (MalformedURLException e) {
			logger.error(""+e, e);
			throw new APIException(e);
		}
	}

	public String getUpdated()
	{
		return(getModified().toString());
	}

	public String getAuthor()
	{
		String retval = "unknown";
		try {
			Credentials creds = null;
			Datastore dstore = Datastore.DS();

			if ((creds = dstore.getCredentials(getUser())) != null){
				retval = creds.getUsername();
			}
		}
		catch (DatastoreException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	public String toXML()
	{
		return(new StringBuilder()
			.append("\t<entry>\n")
			.append("\t\t<title>")
			.append(getTitle())
			.append("</title>\n")
			.append("\t\t<link href=\"")
			.append(getAtomURL())
			.append("\" />\n")
			.append("\t\t<id>")
			.append(""+getUUID())
			.append("</id>\n")
			.append("\t\t<updated>")
			.append(getUpdated())
			.append("</updated>\n")
			.append("\t\t<summary>")
			.append(getDescription())
			.append("</summary>\n")
			.append("\t\t<author>\n")
			.append("\t\t\t<name>")
			.append(getUser())
			.append("</name>\n")
			.append("\t\t</author>\n")
			.append("\t</entry>\n")
			.toString());
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("atomURL=\""+getAtomURL()+"\", ")
			.append("title=\""+getTitle()+"\", ")
			.append("description=\""+getDescription()+"\"")
			.toString());
	}
}
