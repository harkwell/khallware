function init_globals()
{
    window.tagId = 0;
    window.locationsPerPage = 4;
    window.fileitemsPerPage = 15;
    window.bookmarksPerPage = 16;
    window.contactsPerPage = 4;
    window.photosPerPage = 4;
    window.videosPerPage = 2;
    window.eventsPerPage = 4;
    window.blogsPerPage = 20;
    window.audioPerPage = 6;
    window.maxComments = 10;
    window.blogtPerPage = 200;          // blog table
    window.cmttPerPage = 200;           // comment table
    window.gtPerPage = 200;             // groups table
    window.utPerPage = 200;             // users table
    window.btPerPage = 200;             // bookmark table
    window.ptPerPage = 20;              // photo table
    window.ctPerPage = 200;             // contact table
    window.ltPerPage = 200;             // location table
    window.ftPerPage = 200;             // fileitem table
    window.etPerPage = 200;             // event table
    window.atPerPage = 200;             // audio table
    window.vtPerPage = 200;             // video table
    window.vtFromIdx = 0;               // video table
    window.atFromIdx = 0;               // audio table
    window.etFromIdx = 0;               // event table
    window.ftFromIdx = 0;               // fileitem table
    window.ltFromIdx = 0;               // location table
    window.ctFromIdx = 0;               // contact table
    window.ptFromIdx = 0;               // photo table
    window.btFromIdx = 0;               // bookmark table
    window.cmttFromIdx = 0;             // comment table
    window.blogtFromIdx = 0;            // blog table
    window.audioFromIdx = 0;
    window.blogsFromIdx = 0;
    window.eventsFromIdx = 0;
    window.videosFromIdx = 0;
    window.photosFromIdx = 0;
    window.contactsFromIdx = 0;
    window.bookmarksFromIdx = 0;
    window.locationsFromIdx = 0;
    window.fileitemsFromIdx = 0;
    window.queryString = query_string_to_map();
};

function enforce_login()
{
   var cookie = $.cookie('khallware');
   var invalid = (typeof cookie === "undefined");
   var session = getSession();
   invalid = (invalid || !session || !session.valid);

   if (invalid) {
      window.location.replace("/apis/login.html");
   }
}

function attach_content_and_functions()
{
   attach_tag_form();
   attach_blog_form();
   attach_contact_form();
   attach_bookmark_form();
   attach_auto_logout();
}

function attach_tag_form()
{
   $("#new_tag_form").html(""
      +"<span id=\"new_tag_icon\" "
      +   "class=\"glyphicon glyphicon-plus-sign btn-lg black\">"
      +"</span>"
      +"<div id=\"tag_form\">"
      +   "<form>name: <input type=\"text\" id=\"new_tag_name\">"
      +   "</form><div id=\"submit_tag\">create</div>"
      +"</div>");
   $("#new_tag_icon").click(function ()
   {
      $("#tag_form").slideToggle("slow");
   });
   $("#submit_tag").click(function ()
   {
      var name = $("#new_tag_name").val();
      postTag(name, tagId);
      $("#new_tag_name").val('');
      update_tags(tagId, getTags(tagId));
   });
   $("#tag_form").hide();
}

function attach_contact_form()
{
   $("#new_contact_form").html(""
      +"<span id=\"new_contact_icon\" "
      +   "class=\"glyphicon glyphicon-plus-sign btn-lg black\">"
      +"</span>"
      +"<div id=\"contact_form\">"
      +   "<form>name: <input type=\"text\" id=\"new_contact_name\">"
      +   "</form><div id=\"submit_contact\">create</div>"
      +"</div>");
   $("#new_contact_icon").click(function ()
   {
      $("#contact_form").slideToggle("slow");
   });
   $("#submit_contact").click(function ()
   {
      var contact = {
         name : $("#new_contact_name").val()
      };
      postContact(contact);
      $("#new_contact_name").val('');
   });
   $("#contact_form").hide();
}

function attach_blog_form()
{
   $("#new_blog_form").html("<div id=\"new_blog_icon\">new</div>"
      +"<div id=\"blog_form\"><form>"
      +"title: <input type=\"text\" id=\"new_blog_description\">"
      +"entry: <input type=\"text\" id=\"new_blog_content\">"
      +"</form><div id=\"submit_blog\">post</div>"
      +"</div>");
   $("#new_blog_icon").click(function ()
   {
      $("#blog_form").slideToggle("slow");
   });
   $("#submit_blog").click(function ()
   {
      var blog = {};
      blog.content = $("#new_blog_content").val();
      blog.description = $("#new_blog_description").val();
      postBlog(blog, tagId);
      $("#new_blog_content").val('');
      $("#new_blog_description").val('');
   });
   $("#blog_form").hide();
}

function attach_bookmark_form()
{
   $("#new_bookmark_form").html(""
      +"<span id=\"new_bookmark_icon\" "
      +   "class=\"glyphicon glyphicon-plus-sign btn-lg black\">"
      +"</span>"
      +"<div id=\"bookmark_form\">"
      +   "<form>new url: <input type=\"text\" id=\"new_bookmark_url\">"
      +      "name: <input type=\"text\" id=\"new_bookmark_name\">"
      +      "<select id=\"new_bookmark_rating\">"
      +         "<option>unstable</option>"
      +         "<option>annoying</option>"
      +         "<option selected>average</option>"
      +         "<option>good</option>"
      +         "<option>outstanding</option>"
      +      "</select>"
      +   "</form><div id=\"submit_bookmark\">create</div>"
      +"</div>");
   $("#new_bookmark_icon").click(function ()
   {
      $("#bookmark_form").slideToggle("slow");
   });
   $("#submit_bookmark").click(function ()
   {
      var name = $("#new_bookmark_name").val();
      var url = $("#new_bookmark_url").val();
      var rating = $("#new_bookmark_rating").val();
      var bookmark = postBookmark(name, url, rating);
      $("#new_bookmark_name").val('');
      $("#new_bookmark_url").val('');
      $("#new_bookmark_rating").val('');
      updateBookmark(bookmark, tagId);
      update_bookmarks(getBookmarks(0, window.bookmarksPerPage, tagId));
   });
   $("#bookmark_form").hide();
}

function attach_auto_logout(millis)
{
   if (typeof millis === "undefined") {
      millis = (30 * 60 * 1000);
   }
   document.onKeypress = logout_timer_reset;
   document.onMouseover = logout_timer_reset;
   logout_timer_reset();
   window.timer = setTimeout("perform_logout()", millis);
}

function logout_timer_reset()
{
   clearTimeout(window.timer);
}

function perform_logout()
{
   $.removeCookie('khallware');
   window.location.replace("/apis/login.html");
}

function query_string_to_map()
{
   var retval = new Array();

   if (window.location.search.split('?').length > 1) {
      var parms = window.location.search.split('?')[1].split('&');

      for (var i=0; i<parms.length; i++) {
         var key = parms[i].split('=')[0];
         var value = decodeURIComponent(parms[i].split('=')[1]);
         retval[key] = value;
      }
   }
   return(retval);
};

function string_squeeze()
{
   len = 25;
   return function (text, render)
   {
      suffix = "...";

      if (text.length <= len) {
         suffix = "";
      }
      return render(text).substr(0,len)+suffix;
   }
};

function update_photos(photo_list)
{
   photo_list.maxlen = string_squeeze;
   $.get('templates/photo.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#photo_template').html();
      $("#photo_content").html(Mustache.to_html(tmpl, photo_list));
      $(".photo_item").click(function ()
      {
         var winx = $(window).width();
         var winy = $(window).height();
         console.log("photo item clicked");
         $(this).find("#photo_dialog").dialog({
            draggable: false,
            resizable: false,
            height: winy,
            width: winx,
            modal: true
         });
      });
   })
};

function get_tagpath_html(tag)
{
   var retval = "<div class=\"btn-group\" role=\"group\" aria-label=\"tpath\">";
   var list = [];
   var parent = tag;

   while (parent.id > 1) {
      list.push("<button type=\"button\" class=\"btn btn-default\" "
         +"onclick=\"window.location='details.html?tagId="+parent.id+"'\";>"
         +parent.title+"</button>");
      parent = getTag(parent.parent);
   }
   for (var idx=list.length; idx>0; idx--) {
      retval += list[idx-1];
   }
   retval += "</div>";
   return(retval);
}

function update_tags(parentId, tag_list)
{
   var parent = getTag(parentId);
   console.log(parent);
   tag_list.tags = tag_list.tags.slice(0, Math.min(tag_list.tags.length, 24));
   tag_list.maxlen = string_squeeze;
   $("#tagpath_content").html(get_tagpath_html(parent));
   $("#parent_tag").html("<div class=\"tag_item\">"
      +"<i class=\"glyphicon glyphicon-arrow-left bigicon\"></i>"
      +"<a href=\"details.html?tagId="+parent.id+"\">"
      +parent.title+"</a></div>");
   $.get('templates/tag.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#tag_template').html();
      $("#tag_content").html(Mustache.to_html(tmpl, tag_list));
   })
};

function update_contacts(contact_list)
{
   contact_list.maxlen = string_squeeze;
   $.get('templates/contact.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#contact_template').html();
      $("#contact_content").html(Mustache.to_html(tmpl, contact_list));
   })
};

function update_bookmarks(bookmark_list)
{
   bookmark_list.maxlen = string_squeeze;
   $.get('templates/bookmark.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#bookmark_template').html();
      $("#bookmark_content").html(Mustache.to_html(tmpl, bookmark_list));
   })
};

function update_events(event_list)
{
   event_list.maxlen = string_squeeze;
   $.get('templates/event.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#event_template').html();
      $("#event_content").html(Mustache.to_html(tmpl, event_list));
   })
};

function update_locations(location_list)
{
   location_list.maxlen = string_squeeze;
   $.get('templates/location.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#location_template').html();
      $("#location_content").html(Mustache.to_html(tmpl, location_list));
   })
};

function update_fileitems(fileitem_list)
{
   fileitem_list.maxlen = string_squeeze;
   $.get('templates/fileitem.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#fileitem_template').html();
      $("#fileitem_content").html(Mustache.to_html(tmpl, fileitem_list));
   })
};

function update_audio(sound_list)
{
   sound_list.maxlen = string_squeeze;
   $.get('templates/sound.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#sound_template').html();
      $("#audio_content").html(Mustache.to_html(tmpl, sound_list));
   })
};

function update_videos(video_list)
{
   video_list.maxlen = string_squeeze;
   $.get('templates/video.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#video_template').html();
      $("#video_content").html(Mustache.to_html(tmpl, video_list));
      $(".video_item").each(function ()
      {
         $this = $(this);
         var videoId = $this.find("div#id").html();
         // jwplayer("flv_movie_"+videoId).setup({
         //    file: "/apis/v1/videos/"+videoId+".flv",
         //    image: "/apis/v1/videos/"+videoId+".jpg"
         });
      });
   });
};

function update_blogs(blog_list)
{
   var ctmpl = "";
   blog_list.maxlen = string_squeeze;
   $.get('templates/comment.tmpl', function (downloaded)
   {
      ctmpl = $(downloaded).filter('#comment_template').html();
   });
   $.get('templates/blog.tmpl', function (downloaded)
   {
      var btmpl = $(downloaded).filter('#blog_template').html();
      $("#blog_content").html(Mustache.to_html(btmpl, blog_list));
      $(".blog_item").each(function ()
      {
         $this = $(this);
         var blogId = $this.find("div#id").html();
         var list = getComments(0, maxComments, blogId);
         $this.find(".comment_content").append(Mustache.to_html(ctmpl, list));
      });
      $("div.add_comment_icon").each(function ()
      {
         $(this).click(function ()
         {
            $(this).parent().find("div.add_comment_form").slideToggle("slow");
         });
      });
      $("div.add_comment_form").find("#submit_add_comment").each(function ()
      {
         $(this).click(function ()
         {
            var cmt = {};
            var blog = {};
            blog.id = $(this).parent().find("input#blogId").val();
            cmt.description = $(this).parent().find("#new_add_cmt_desc").val();
            cmt.content = $(this).parent().find("#new_add_cmt_content").val();
            cmt.blog = blog;
            postComment(cmt);
            $this.find("#new_add_cmt_content").val('');
            $this.find("#new_add_cmt_desc").val('');
         });
      });
   });
};

function next_page_clicked(handler, page, pageSize, tagId)
{
   console.log("next page link clicked");

   if (typeof page === "undefined") {
      page = 0;
   }
   if (typeof pageSize === "undefined") {
      pageSize = 20;
   }
   handler(page, pageSize, tagId);
   // window.scrollTo(0, 0);
};

function prev_page_clicked(handler, page, pageSize, tagId)
{
   console.log("previous page link clicked");

   if (typeof page === "undefined") {
      page = 0;
   }
   if (typeof pageSize === "undefined") {
      pageSize = 20;
   }
   if (typeof tagId === "undefined") {
      tagId = -1;
   }
   handler(page, pageSize, tagId);
};

function get_photo_page(page, pageSize, tagId)
{
   console.log("there were "+$(".photo_item").length+" photos displayed");

   if (true || $(".photo_item").length > 0) {
      update_photos(getPhotos(page, pageSize, tagId));
   }
};

function get_bookmark_page(page, pageSize, tagId)
{
   console.log("there were "+$(".bookmark_item").length+" bookmarks displayed");

   if (true || $(".bookmark_item").length > 0) {
      update_bookmarks(getBookmarks(page, pageSize, tagId));
   }
};

function get_contact_page(page, pageSize, tagId)
{
   console.log("there were "+$(".contact_item").length+" contacts displayed");

   if (true || $(".contact_item").length > 0) {
      update_contacts(getContacts(page, pageSize, tagId));
   }
};

function get_video_page(page, pageSize, tagId)
{
   console.log("there were "+$(".video_item").length+" videos displayed");

   if (true || $(".video_item").length > 0) {
      update_videos(getVideos(page, pageSize, tagId));
   }
};

function get_audio_page(page, pageSize, tagId)
{
   console.log("there were "+$(".audio_item").length+" sounds displayed");

   if (true || $(".sound_item").length > 0) {
      update_audio(getSounds(page, pageSize, tagId));
   }
};

function get_event_page(page, pageSize, tagId)
{
   console.log("there were "+$(".event_item").length+" events displayed");

   if (true || $(".event_item").length > 0) {
      update_events(getEvents(page, pageSize, tagId));
   }
};

function get_location_page(page, pageSize, tagId)
{
   console.log("there were "+$(".location_item").length+" locations displayed");

   if (true || $(".location_item").length > 0) {
      update_locations(getLocations(page, pageSize, tagId));
   }
};

function get_fileitem_page(page, pageSize, tagId)
{
   console.log("there were "+$(".fileitem_item").length+" fileitems displayed");

   if (true || $(".fileitem_item").length > 0) {
      update_fileitems(getFileItems(page, pageSize, tagId));
   }
};

function get_bookmarktable_page(page, pageSize)
{
   console.log("were "+$(".bookmarktable_item").length+" bookmarks displayed");

   if (true || $(".bookmarktable_item").length > 0) {
      update_bookmarks_table(getBookmarks(page, pageSize));
   }
};

function get_phototable_page(page, pageSize)
{
   console.log("there were "+$(".phototable_item").length+" photos displayed");

   if (true || $(".phototable_item").length > 0) {
      update_photos_table(getPhotos(page, pageSize));
   }
};

function get_videotable_page(page, pageSize)
{
   console.log("there were "+$(".videotable_item").length+" videos displayed");

   if (true || $(".videotable_item").length > 0) {
      update_videos_table(getVideos(page, pageSize));
   }
};

function get_audiotable_page(page, pageSize)
{
   console.log("there were "+$(".audiotable_item").length+" audio displayed");

   if (true || $(".audiotable_item").length > 0) {
      update_audio_table(getSounds(page, pageSize));
   }
};

function get_eventtable_page(page, pageSize)
{
   console.log("there were "+$(".eventtable_item").length+" events displayed");

   if (true || $(".eventtable_item").length > 0) {
      update_events_table(getEvents(page, pageSize));
   }
};

function get_locationtable_page(page, pageSize)
{
   console.log("were "+$(".locationtable_item").length+" locations displayed");

   if (true || $(".locationtable_item").length > 0) {
      update_locations_table(getLocations(page, pageSize));
   }
};

function get_blogtable_page(page, pageSize)
{
   console.log("there were "+$(".blogtable_item").length+" blogs displayed");

   if (true || $(".blogtable_item").length > 0) {
      update_blogs_table(getBlogs(page, pageSize));
   }
};

function get_contacttable_page(page, pageSize)
{
   console.log("were "+$(".contacttable_item").length+" contacts displayed");

   if (true || $(".contacttable_item").length > 0) {
      update_contacts_table(getContacts(page, pageSize));
   }
};

function get_usertable_page(page, pageSize)
{
   console.log("were "+$(".usertable_item").length+" users displayed");

   if (true || $(".usertable_item").length > 0) {
      update_users_table(getUsers(page, pageSize));
   }
};

function get_grouptable_page(page, pageSize)
{
   console.log("were "+$(".grouptable_item").length+" groups displayed");

   if (true || $(".grouptable_item").length > 0) {
      update_groups_table(getGroups(page, pageSize));
   }
};

function get_sessiontable_page(page, pageSize)
{
   console.log("were "+$(".sessiontable_item").length+" sessions displayed");

   if (true || $(".sessiontable_item").length > 0) {
      update_sessions_table(getGroups(page, pageSize));
   }
};

function perform_selected(callback)
{
   $("tr.tr_item").each(function ()
   {
      $this = $(this);
      var id = $this.find("span.item_id").html();
      var isSelected = $this.find(":checkbox").is(':checked');

      if (isSelected) {
         console.log("item "+id+" is selected... callback.");
         callback(id);
      }
   });
}

function update_bookmark_tag(id)
{
   var tagId = $("#given_bookmark_tag").val();
   var bookmark = getBookmark(id);
   console.log("updating bookmark "+id+" with tag id "+tagId);
   updateBookmark(bookmark, tagId)
}

function update_bookmark_rating(id)
{
   var bookmark = getBookmark(id);
   bookmark.rating = $("#given_bookmark_rating").val();
   console.log("updating bookmark "+id+" with rating "+bookmark.rating);
   updateBookmark(bookmark)
}

function update_bookmark_mask(id)
{
   var bookmark = getBookmark(id);
   bookmark.mask = $("#given_bookmark_mask").val();
   console.log("updating bookmark "+id+" with mask "+bookmark.mask);
   updateBookmark(bookmark)
}

function update_photo_mask(id)
{
   var photo = getPhoto(id);
   photo.mask = $("#given_photo_mask").val();
   console.log("updating photo "+photo.id+" with mask "+photo.mask);
   updatePhoto(photo)
}

function update_contact_mask(id)
{
   var contact = getContact(id);
   contact.mask = $("#given_contact_mask").val();
   console.log("updating contact "+contact.id+" with mask "+contact.mask);
   updateContact(contact)
}

function update_audio_mask(id)
{
   var sound = getSound(id);
   sound.mask = $("#given_audio_mask").val();
   console.log("updating sound "+sound.id+" with mask "+sound.mask);
   updateSound(sound)
}

function update_video_mask(id)
{
   var video = getVideo(id);
   video.mask = $("#given_video_mask").val();
   console.log("updating video "+video.id+" with mask "+video.mask);
   updateVideo(video)
}

function update_fileitem_mask(id)
{
   var fileitem = getFileitem(id);
   fileitem.mask = $("#given_fileitem_mask").val();
   console.log("updating fileitem "+fileitem.id+" with mask "+fileitem.mask);
   updateFileitem(fileitem)
}

function update_event_mask(id)
{
   var event = getEvent(id);
   event.mask = $("#given_event_mask").val();
   console.log("updating event "+event.id+" with mask "+event.mask);
   updateEvent(event)
}

function update_location_mask(id)
{
   var location = getLocation(id);
   location.mask = $("#given_location_mask").val();
   console.log("updating location "+location.id+" with mask "+location.mask);
   updateLocation(location)
}

function update_blog_mask(id)
{
   var blog = getBlog(id);
   blog.mask = $("#given_blog_mask").val();
   console.log("updating blog "+blog.id+" with mask "+blog.mask);
   updateBlog(blog)
}

function update_photo_tag(id)
{
   var tagId = $("#given_photo_tag").val();
   var photo = getPhoto(id);
   console.log("updating photo "+id+" with tag id "+tagId);
   updatePhoto(photo, tagId)
}

// http://stackoverflow.com/questions/5907645/jquery-chrome-and-checkboxes-strange-behavior
function fix_chromium_checkbox_bug()
{
      return; // KDH this seems to be broken, too
      $(':checkbox').not(".selector").click(function (e)
      {
         var value = this.checked;
         $(':checkbox').each(function () { this.checked = value; });
      });
}

function handle_select_all()
{
   isChecked = $(this).find(":checkbox").is(':checked');
   $("tr.tr_item").each(function ()
   {
      $(this).find(":checkbox").not(".selector").attr('checked', isChecked);
   })
};

function update_bookmarks_table(bookmark_list)
{
   bookmark_list.maxlen = string_squeeze;
   $.get('templates/bookmarktable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#bookmarktable_template').html();
      var hdr = "<table id=\"t_bookmark\"><tr><th id=\"t_bookmark_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_bookmark_id\">id</th>"
               +"<th id=\"t_bookmark_name\">name</th>"
               +"<th id=\"t_bookmark_url\">url</th>"
               +"<th id=\"t_bookmark_desc\">description</th>"
               +"<th id=\"t_bookmark_date\">date</th>"
               +"<th id=\"t_bookmark_mask\">mask</th>"
               +"<th id=\"t_bookmark_rating\">rating</th><th>delete</th></tr>";
      $("#bookmarktable_content").html(hdr+Mustache.to_html(tmpl,
         bookmark_list)+"</table>");
      fix_chromium_checkbox_bug();
      $('#t_bookmark').tablesorter();
      $('#t_bookmark_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_photos_table(photo_list)
{
   photo_list.maxlen = string_squeeze;
   $.get('templates/phototable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#phototable_template').html();
      var hdr = "<table id=\"t_photo\"><tr><th id=\"t_photo_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_photo_id\">id</th><th>thumbnail</th>"
               +"<th id=\"t_photo_name\">name</th>"
               +"<th id=\"t_photo_path\">path</th>"
               +"<th id=\"t_photo_date\">date</th>"
               +"<th id=\"t_photo_desc\">description</th>"
               +"<th id=\"t_photo_group\">group</th>"
               +"<th id=\"t_photo_mask\">mask</th><th>delete</th></tr>";
      $("#phototable_content").html(hdr+Mustache.to_html(tmpl, photo_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_photo').tablesorter();
      $('#t_photo_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_contacts_table(contact_list)
{
   contact_list.maxlen = string_squeeze;
   $.get('templates/contacttable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#contacttable_template').html();
      var hdr = "<table id=\"t_contact\"><tr><th id=\"t_contact_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_contact_id\">id</th>"
               +"<th id=\"t_contact_name\">name</th>"
               +"<th id=\"t_contact_desc\">description</th>"
               +"<th id=\"t_contact_date\">date</th>"
               +"<th id=\"t_contact_group\">group</th>"
               +"<th id=\"t_contact_mask\">mask</th><th>delete</th></tr>";
      $("#contacttable_content").html(hdr+Mustache.to_html(tmpl, contact_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_contact').tablesorter();
      $('#t_contact_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_events_table(event_list)
{
   event_list.maxlen = string_squeeze;
   $.get('templates/eventtable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#eventtable_template').html();
      var hdr = "<table id=\"t_event\"><tr><th id=\"t_event_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_event_id\">id</th>"
               +"<th id=\"t_event_name\">name</th>"
               +"<th id=\"t_event_desc\">description</th>"
               +"<th id=\"t_event_start\">start time</th>"
               +"<th id=\"t_event_end\">end time</th>"
               +"<th id=\"t_event_cur\">duration</th>"
               +"<th id=\"t_event_group\">group</th>"
               +"<th id=\"t_event_mask\">mask</th><th>delete</th></tr>";
      $("#eventtable_content").html(hdr+Mustache.to_html(tmpl, event_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_event').tablesorter();
      $('#t_event_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_videos_table(video_list)
{
   video_list.maxlen = string_squeeze;
   $.get('templates/videotable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#videotable_template').html();
      var hdr = "<table id=\"t_video\"><tr><th id=\"t_video_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_video_id\">id</th>"
               +"<th id=\"t_video_name\">name</th><th>download</th>"
               +"<th id=\"t_video_path\">path</th>"
               +"<th id=\"t_video_desc\">description</th>"
               +"<th id=\"t_video_date\">date</th>"
               +"<th id=\"t_video_mask\">mask</th><th>delete</th></tr>";
      $("#videotable_content").html(hdr+Mustache.to_html(tmpl, video_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_video').tablesorter();
      $('#t_video_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_audio_table(sound_list)
{
   sound_list.maxlen = string_squeeze;
   $.get('templates/soundtable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#soundtable_template').html();
      var hdr = "<table id=\"t_sound\"><tr><th id=\"t_sound_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_sound_id\">id</th>"
               +"<th id=\"t_sound_name\">name</th><th>download</th>"
               +"<th id=\"t_sound_path\">path</th>"
               +"<th id=\"t_sound_desc\">description</th>"
               +"<th id=\"t_sound_artist\">artist</th>"
               +"<th id=\"t_sound_title\">title</th>"
               +"<th id=\"t_sound_album\">album</th>"
               +"<th id=\"t_sound_genre\">genre</th>"
               +"<th id=\"t_sound_date\">date</th>"
               +"<th id=\"t_sound_group\">group</th>"
               +"<th id=\"t_sound_mask\">mask</th><th>delete</th></tr>";
      $("#audiotable_content").html(hdr+Mustache.to_html(tmpl, sound_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_sound').tablesorter();
      $('#t_sound_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_locations_table(location_list)
{
   location_list.maxlen = string_squeeze;
   $.get('templates/locationtable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#locationtable_template').html();
      var hdr = "<table id=\"t_loc\"><tr><th id=\"t_loc_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_loc_id\">id</th>"
               +"<th id=\"t_loc_name\">name</th><th>download</th>"
               +"<th id=\"t_loc_path\">path</th>"
               +"<th id=\"t_loc_desc\">description</th>"
               +"<th id=\"t_loc_date\">date</th>"
               +"<th id=\"t_loc_group\">group</th>"
               +"<th id=\"t_loc_mask\">mask</th><th>delete</th></tr>";
      $("#locationtable_content").html(hdr+Mustache.to_html(tmpl, location_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_loc').tablesorter();
      $('#t_loc_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_fileitems_table(fileitem_list)
{
   fileitem_list.maxlen = string_squeeze;
   $.get('templates/fileitemtable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#fileitemtable_template').html();
      var hdr = "<table id=\"t_fileitem\"><tr><th id=\"t_fileitem_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_fileitem_id\">id</th>"
               +"<th id=\"t_fileitem_name\">name</th>"
               +"<th id=\"t_fileitem_mime\">mime</th><th>download</th>"
               +"<th id=\"t_fileitem_path\">path</th>"
               +"<th id=\"t_fileitem_date\">date</th>"
               +"<th id=\"t_fileitem_desc\">description</th>"
               +"<th id=\"t_fileitem_group\">group</th>"
               +"<th id=\"t_fileitem_mask\">mask</th><th>delete</th></tr>";
      $("#fileitemtable_content").html(hdr+Mustache.to_html(tmpl, fileitem_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_fileitem').tablesorter();
      $('#t_fileitem_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_blogs_table(blog_list)
{
   blog_list.maxlen = string_squeeze;
   $.get('templates/blogtable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#blogtable_template').html();
      var hdr = "<table id=\"t_blog\"><tr><th id=\"t_blog_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_blog_id\">id</th>"
               +"<th id=\"t_blog_user\">user</th>"
               +"<th id=\"t_blog_desc\">description</th>"
               +"<th id=\"t_blog_content\">content</th>"
               +"<th id=\"t_blog_date\">date</th>"
               +"<th id=\"t_blog_comments\">comments</th>"
               +"<th id=\"t_blog_group\">group</th>"
               +"<th id=\"t_blog_mask\">mask</th><th>delete</th></tr>";
      $("#blogtable_content").html(hdr+Mustache.to_html(tmpl, blog_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_blog').tablesorter();
      $('#t_blog_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_comments_table(comment_list)
{
   comment_list.maxlen = string_squeeze;
   $.get('templates/commenttable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#commenttable_template').html();
      var hdr = "<table id=\"t_cmt\"><tr><th id=\"t_cmt_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_cmt_id\">id</th>"
               +"<th id=\"t_cmt_user\">user</th>"
               +"<th id=\"t_cmt_content\">content</th>"
               +"<th id=\"t_cmt_date\">date</th>"
               +"<th id=\"t_cmt_group\">group</th>"
               +"<th id=\"t_cmt_mask\">mask</th><th>delete</th></tr>";
      $("#commenttable_content").html(hdr+Mustache.to_html(tmpl, comment_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_cmt').tablesorter();
      $('#t_cmt_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_users_table(user_list)
{
   user_list.maxlen = string_squeeze;
   $.get('../templates/usertable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#usertable_template').html();
      var hdr = "<table id=\"t_usr\"><tr><th id=\"t_cmt_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_usr_id\">id</th>"
               +"<th id=\"t_usr_username\">username</th>"
               +"<th id=\"t_usr_quota\">quota</th>"
               +"<th id=\"t_usr_disabled\">disabled</th>"
               +"<th id=\"t_usr_regikey\">registration key</th>"
               +"<th id=\"t_usr_group\">group</th>"
               +"<th id=\"t_usr_email\">email</th>"
               +"<th>disable</th></tr>";
      $("#userstable_content").html(hdr+Mustache.to_html(tmpl, user_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_usr').tablesorter();
      $('#t_usr_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_groups_table(group_list)
{
   group_list.maxlen = string_squeeze;
   $.get('../templates/grouptable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#grouptable_template').html();
      var hdr = "<table id=\"t_grp\"><tr><th id=\"t_cmt_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_grp_id\">id</th>"
               +"<th id=\"t_grp_groupname\">name</th>"
               +"<th id=\"t_grp_disabled\">disabled</th>"
               +"<th id=\"t_grp_desc\">description</th>"
               +"<th>disable</th></tr>";
      $("#groupstable_content").html(hdr+Mustache.to_html(tmpl, group_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_grp').tablesorter();
      $('#t_grp_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_sessions_table(session_list)
{
   session_list.maxlen = string_squeeze;
   $.get('../templates/sessiontable.tmpl', function (downloaded)
   {
      var tmpl = $(downloaded).filter('#sessiontable_template').html();
      var hdr = "<table id=\"t_ses\"><tr><th id=\"t_ses_select_all\">"
               +"<input class=\"selector\" type=\"checkbox\"></th>"
               +"<th id=\"t_ses_id\">id</th>"
               +"<th id=\"t_ses_name\">name</th>"
               +"<th id=\"t_ses_disabled\">disabled</th>"
               +"<th id=\"t_ses_desc\">description</th>"
               +"<th id=\"t_ses_user\">username</th>"
               +"<th id=\"t_ses_email\">email</th>"
               +"<th id=\"t_ses_group\">group</th>"
               +"<th id=\"t_ses_uuid\">uuid</th>"
               +"<th>remove</th></tr>";
      $("#sessionstable_content").html(hdr+Mustache.to_html(tmpl, session_list)
         +"</table>");
      fix_chromium_checkbox_bug();
      $('#t_ses').tablesorter();
      $('#t_ses_select_all')
         .click(handle_select_all)
         .wrapInner('<span title="select all"/>');
   })
};

function update_contact_tag(id)
{
   var tagId = $("#given_contact_tag").val();
   var contact = getContact(id);
   console.log("updating contact "+id+" with tag id "+tagId);
   updateContact(contact, tagId)
}

function update_video_tag(id)
{
   var tagId = $("#given_video_tag").val();
   var video = getVideo(id);
   console.log("updating video "+id+" with tag id "+tagId);
   updateVideo(video, tagId)
}

function update_audio_tag(id)
{
   var tagId = $("#given_audio_tag").val();
   var sound = getSound(id);
   console.log("updating sound "+id+" with tag id "+tagId);
   updateSound(sound, tagId)
}

function update_event_tag(id)
{
   var tagId = $("#given_event_tag").val();
   var event = getEvent(id);
   console.log("updating event "+id+" with tag id "+tagId);
   updateEvent(event, tagId)
}

function update_location_tag(id)
{
   var tagId = $("#given_location_tag").val();
   var location = getLocation(id);
   console.log("updating location "+id+" with tag id "+tagId);
   updateLocation(location, tagId)
}

function update_blog_tag(id)
{
   var tagId = $("#given_blog_tag").val();
   var blog = getBlog(id);
   console.log("updating blog "+id+" with tag id "+tagId);
   updateBlog(blog, tagId)
}

function update_fileitem_tag(id)
{
   var tagId = $("#given_fileitem_tag").val();
   var fileitem = getFileItem(id);
   console.log("updating fileitem "+id+" with tag id "+tagId);
   updateFileItem(fileitem, tagId)
}

function toggleThumb(id)
{
   $("tr.tr_item").each(function ()
   {
      $this = $(this);
      var found = $this.find("span.item_id").html();

      if (found == id) {
         // console.log("found item "+id);
         $this.find("img").slideToggle();
      }
   });
}

function get_file_item(id, ext)
{
   if (typeof mime === "undefined") {
      ext = "";
   }
   else {
      ext = "."+ext;
   }
   var url = "/apis/v1/fileitems/"+id+ext;
   $(".fileitem_content").append("<iframe src=\""+url+"\"></iframe>");
}

function init_details_page(tagId)
{
   setTimeout(function()
   {
      $.when(getTag(tagId), getTags(tagId)).then(function (tag, tags)
      {
         $("#tagDescription").html("<div>"+tag.title.toUpperCase()+"</div>");
         update_tags(tag.parent, tags);
      });
   }, 0);
   setTimeout(function()
   {
      $.when(getPhotos(0, photosPerPage, tagId)).then(update_photos);
   }, 0);
   setTimeout(function()
   {
      $.when(getVideos(0, videosPerPage, tagId)).then(update_videos);
   }, 0);
   setTimeout(function()
   {
      $.when(getSounds(0, audioPerPage, tagId)).then(update_audio);
   }, 0);
   setTimeout(function()
   {
      $.when(getBookmarks(0, bookmarksPerPage, tagId)).then(update_bookmarks);
   }, 0);
   setTimeout(function()
   {
      $.when(getLocations(0, locationsPerPage, tagId)).then(update_locations);
   }, 0);
   setTimeout(function()
   {
      $.when(getFileItems(0, fileitemsPerPage, tagId)).then(update_fileitems);
   }, 0);
   setTimeout(function()
   {
      $.when(getContacts(0, contactsPerPage, tagId)).then(update_contacts);
   }, 0);
   setTimeout(function()
   {
      $.when(getEvents(0, eventsPerPage, tagId)).then(update_events);
   }, 0);
   setTimeout(function()
   {
      $.when(getBlogs(0, blogsPerPage, tagId)).then(update_blogs);
   }, 0);
}
