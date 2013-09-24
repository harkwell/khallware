-- $Id$
-- ============================================================================
-- This script creates the structure of the webapp database.
--
-- mysql -Umysql mysql <db_schema.sql
-- ============================================================================
SET foreign_key_checks = 0;
DROP TABLE IF EXISTS bookmarks, tags, bookmark_tags, sound_tags, location_tags, fileitem_tags, event_tags, video_tags, blog_tags, contact_tags, photo_tags, edges, locations, contacts, events, credentials, videos, photos, fileitems, sounds, blogs, comments, sessions, groups;
SET foreign_key_checks = 1;
CREATE TABLE groups (
	id INT(11) NOT NULL AUTO_INCREMENT,
	name VARCHAR(1024) NOT NULL,
	description VARCHAR(1024) NOT NULL,
	disabled BIT(1) NOT NULL DEFAULT 0,
	PRIMARY KEY (id)
);
CREATE TABLE credentials (
	id INT(11) NOT NULL AUTO_INCREMENT,
	username VARCHAR(1024) NOT NULL,
	password VARCHAR(1024) NOT NULL,
	email VARCHAR(1024) NOT NULL,
	regikey VARCHAR(1024),
	_group INT(11) NOT NULL,
	disabled BIT(1) NOT NULL DEFAULT 0,
	PRIMARY KEY (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
		ON DELETE CASCADE
);
CREATE TABLE bookmarks (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(1024),
        user INT(11),
        url VARCHAR(1024) NOT NULL,
	_group INT(11),
        mask INT(11) NOT NULL DEFAULT 700,
        modified DATETIME DEFAULT NULL,
        rating VARCHAR(1024) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(1024) NOT NULL,
        user INT(11),
	_group INT(11),
        parent INT(11) NOT NULL DEFAULT 0,
        mask INT(11) NOT NULL DEFAULT 700,
        modified DATETIME DEFAULT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE locations (
        id INT(11) NOT NULL AUTO_INCREMENT,
        latitude decimal(8,6) NOT NULL,
        longitude decimal(9,6) NOT NULL,
        name VARCHAR(1024) NOT NULL,
        user INT(11),
	_group INT(11),
        address VARCHAR(1024),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
        modified DATETIME DEFAULT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE contacts (
        id INT(11) NOT NULL AUTO_INCREMENT,
        uid VARCHAR(1024) DEFAULT NULL,
        name VARCHAR(1024) NOT NULL,
        email VARCHAR(1024),
        phone VARCHAR(1024),
        title VARCHAR(1024),
        address VARCHAR(1024),
        organization VARCHAR(1024),
        vcard BLOB NOT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        modified DATETIME DEFAULT NULL,
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE events (
        id INT(11) NOT NULL AUTO_INCREMENT,
        uid VARCHAR(1024) DEFAULT NULL,
        duration INT(11) NOT NULL,
        name VARCHAR(1024) NOT NULL,
        start DATETIME DEFAULT NULL,
        end DATETIME DEFAULT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        ics BLOB NOT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE photos (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(1024) NOT NULL,
        path VARCHAR(1024) NOT NULL,
        md5sum VARCHAR(32) DEFAULT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE fileitems (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(1024) NOT NULL,
        ext VARCHAR(1024) NOT NULL,
        mime VARCHAR(1024),
        path VARCHAR(1024) NOT NULL,
        md5sum VARCHAR(32) DEFAULT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE sounds (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(1024) NOT NULL,
        path VARCHAR(1024) NOT NULL,
        md5sum VARCHAR(32) DEFAULT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        recording DATETIME DEFAULT NULL,
        user INT(11),
        title VARCHAR(1024),
        artist VARCHAR(1024),
        genre VARCHAR(1024),
        album VARCHAR(1024),
        publisher VARCHAR(1024),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE videos (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(1024) NOT NULL,
        path VARCHAR(1024) NOT NULL,
        md5sum VARCHAR(32) DEFAULT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE blogs (
        id INT(11) NOT NULL AUTO_INCREMENT,
        content VARCHAR(1024) NOT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE comments (
        id INT(11) NOT NULL AUTO_INCREMENT,
        blog_id INT(11) NOT NULL,
        content VARCHAR(1024) NOT NULL,
        created DATETIME DEFAULT NULL,
        modified DATETIME DEFAULT NULL,
        user INT(11),
	_group INT(11),
        description VARCHAR(1024),
        mask INT(11) NOT NULL DEFAULT 700,
	PRIMARY KEY (id),
	FOREIGN KEY (blog_id)
		REFERENCES blogs (id)
		ON DELETE CASCADE,
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE sessions (
	id INT(11) NOT NULL AUTO_INCREMENT,
	credential_id INT(11) NOT NULL,
	name VARCHAR(1024) NOT NULL,
	_group INT(11),
        modified DATETIME DEFAULT NULL,
        mask INT(11) NOT NULL DEFAULT 700,
        user INT(11),
	PRIMARY KEY (id),
	FOREIGN KEY (credential_id)
		REFERENCES credentials (id)
		ON DELETE CASCADE,
	FOREIGN KEY (user)
		REFERENCES credentials (id),
	FOREIGN KEY (_group)
		REFERENCES groups (id)
);
CREATE TABLE bookmark_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        bookmark INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (bookmark)
		REFERENCES bookmarks (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE photo_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        photo INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (photo)
		REFERENCES photos (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE fileitem_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        fileitem INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (fileitem)
		REFERENCES fileitems (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE sound_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        sound INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (sound)
		REFERENCES sounds (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE video_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        video INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (video)
		REFERENCES videos (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE blog_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        blog INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (blog)
		REFERENCES blogs (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE contact_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        contact INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (contact)
		REFERENCES contacts (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE event_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        event INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (event)
		REFERENCES events (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE location_tags (
        id INT(11) NOT NULL AUTO_INCREMENT,
        location INT(11) NOT NULL,
        tag INT(11) NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (location)
		REFERENCES locations (id)
		ON DELETE CASCADE,
	FOREIGN KEY (tag)
		REFERENCES tags (id)
		ON DELETE CASCADE
);
CREATE TABLE landing (
	url VARCHAR(1024) NOT NULL,
	_group INT(11) NOT NULL,
	FOREIGN KEY (_group)
		REFERENCES groups (id)
		ON DELETE CASCADE
);
-- NULL parents belong to ROOT group
CREATE TABLE edges (
        _group INT(11),
        parent INT(11),
	FOREIGN KEY (_group)
		REFERENCES groups (id),
	FOREIGN KEY (parent)
		REFERENCES groups (id)
);
CREATE TABLE quota (
        user INT(11) NOT NULL,
        available INT(11) NOT NULL,
        used INT(11) NOT NULL,
	FOREIGN KEY (user)
		REFERENCES credentials (id)
		ON DELETE CASCADE
);
CREATE TABLE last_login (
        user INT(11) NOT NULL,
        began DATETIME NOT NULL,
        agent VARCHAR(1024) DEFAULT NULL,
	FOREIGN KEY (user)
		REFERENCES credentials (id)
		ON DELETE CASCADE
);
CREATE OR REPLACE VIEW search_blogs_view AS
	SELECT tags.id AS tag, bl.id AS id, 'blog' AS class,
		CONCAT(bl.content,' ',bl.description) AS haystack,
		bl.mask, bl.user, bl._group
	FROM tags, blogs bl
	WHERE bl.id IN (
		SELECT blog
		FROM blog_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_bookmarks_view AS
	SELECT tags.id AS tag, bk.id AS id, 'bookmark' AS class,
		CONCAT(bk.name,' ',bk.url,' ',bk.rating) AS haystack,
		bk.mask, bk.user, bk._group
	FROM tags, bookmarks bk
	WHERE bk.id IN (
		SELECT bookmark
		FROM bookmark_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_contacts_view AS
	SELECT tags.id AS tag, ct.id AS id, 'contact' AS class,
		CONCAT(ct.name,' ',ct.email,' ',ct.phone,' ',
			ct.title,' ',ct.address,' ',ct.organization,' ',
			ct.description) AS haystack,
			ct.mask, ct.user, ct._group
	FROM tags, contacts ct
	WHERE ct.id IN (
		SELECT contact
		FROM contact_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_events_view AS
	SELECT tags.id AS tag, ev.id AS id, 'event' AS class,
		CONCAT(ev.name,' ',ev.description) AS haystack,
			ev.mask, ev.user, ev._group
	FROM tags, events ev
	WHERE ev.id IN (
		SELECT event
		FROM event_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_fileitems_view AS
	SELECT tags.id AS tag, fi.id AS id, 'fileitem' AS class,
		CONCAT(fi.name,' ',fi.path,' ',fi.description) AS haystack,
			fi.mask, fi.user, fi._group
	FROM tags, fileitems fi
	WHERE fi.id IN (
		SELECT fileitem
		FROM fileitem_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_locations_view AS
	SELECT tags.id AS tag, lo.id AS id, 'location' AS class,
		CONCAT(lo.name,' ',lo.latitude,',',lo.longitude,' ',
			lo.address,' ',lo.description) AS haystack,
			lo.mask, lo.user, lo._group
	FROM tags, locations lo
	WHERE lo.id IN (
		SELECT location
		FROM location_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_photos_view AS
	SELECT tags.id AS tag, ph.id AS id, 'photo' AS class,
		CONCAT(ph.name,' ',ph.path,' ',ph.description) AS haystack,
			ph.mask, ph.user, ph._group
	FROM tags, photos ph
	WHERE ph.id IN (
		SELECT photo
		FROM photo_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_sounds_view AS
	SELECT tags.id AS tag, so.id AS id, 'sound' AS class,
		CONCAT(so.name,' ',so.path,' ',so.description,' ',
			so.title,' ',so.artist,' ',so.genre,' ',
			so.album,' ',so.publisher) AS haystack,
			so.mask, so.user, so._group
	FROM tags, sounds so
	WHERE so.id IN (
		SELECT sound
		FROM sound_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_videos_view AS
	SELECT tags.id AS tag, vi.id AS id, 'sound' AS class,
		CONCAT(vi.name,' ',vi.path,' ',vi.description) AS haystack,
			vi.mask, vi.user, vi._group
	FROM tags, sounds vi
	WHERE vi.id IN (
		SELECT video
		FROM video_tags
		WHERE tag = tags.id);
CREATE OR REPLACE VIEW search_view AS
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_blogs_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_bookmarks_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_contacts_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_events_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_fileitems_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_locations_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_photos_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_sounds_view
	UNION
	SELECT DISTINCT tag, id, class, haystack, mask, user, _group
	FROM search_videos_view;
