function postTag(name, parent)
{
   var json = "{\"name\": \""+name+"\", \"parent\": \""+parent+"\"}";
   console.log("posting json: "+json);
   $.ajax({
      type: "POST",
      url: "/apis/v1/tags",
      data: ""+json,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
};

function postContact(contact)
{
   var retval;
   var json = JSON.stringify(contact);

   console.log("posting json: "+json);
   $.ajax({
      type: "POST",
      url: "/apis/v1/contacts",
      data: ""+json,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
};

function postBookmark(name, url, rating)
{
   var retval;
   var json = "{\"name\": \""+name+"\", \"url\": \""+url+"\", "
             +"\"rating\": \""+rating+"\"}";

   console.log("posting json: "+json);
   $.ajax({
      type: "POST",
      url: "/apis/v1/bookmarks",
      data: ""+json,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
};

function postBlog(blog, tagId)
{
   var retval;
   var json = JSON.stringify(blog);
   var suffix = "?tagId="+tagId;

   if (typeof tagId === "undefined") {
      suffix = "";
   }
   console.log("posting json: "+json);
   $.ajax({
      type: "POST",
      url: "/apis/v1/blogs"+suffix,
      data: json,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
};

function postComment(comment)
{
   var retval;
   var json = JSON.stringify(comment);
   console.log("posting json: "+json);
   $.ajax({
      type: "POST",
      url: "/apis/v1/blogs/"+comment.blog.id+"/comments",
      data: json,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
};

function deleteTag(tagId)
{
   console.log("deleting tag: "+tagId);
   $.ajax({
      type: "DELETE",
      url: "/apis/v1/tags/"+tagId,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
};

function deletePhoto(photoId)
{
   console.log("deleting photo: "+photoId);
   $.ajax({
      type: "DELETE",
      url: "/apis/v1/photos/"+photoId,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
};

function deleteSound(soundId)
{
   console.log("deleting sound: "+soundId);
   $.ajax({
      type: "DELETE",
      url: "/apis/v1/sounds/"+soundId,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
};

function updateItem(item_type, item, tagId)
{
   var suffix = "?tagId="+tagId;
   var json = JSON.stringify(item);
   var uri = "/apis/v1/"+item_type+"s/"+item.id;
   console.log("put-ing json: "+json);

   if (typeof tagId === "undefined") {
      suffix = "";
   }
   $.ajax({
      type: "PUT",
      url: uri+suffix,
      data: ""+json,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
}

function updateBookmark(bookmark, tagId)
{
   updateItem("bookmark", bookmark, tagId);
};

function updatePhoto(photo, tagId)
{
   updateItem("photo", photo, tagId);
};

function updateContact(contact, tagId)
{
   updateItem("contact", contact, tagId);
};

function updateVideo(video, tagId)
{
   updateItem("video", video, tagId);
};

function updateFileItem(fileitem, tagId)
{
   updateItem("fileitem", fileitem, tagId);
};

function updateSound(sound, tagId)
{
   updateItem("sound", sound, tagId);
};

function updateEvent(event, tagId)
{
   updateItem("event", event, tagId);
};

function updateLocation(location, tagId)
{
   updateItem("location", location, tagId);
};

function updateBlog(blog, tagId)
{
   updateItem("blog", blog, tagId);
};

function deleteItem(item_type, itemId)
{
   var uri = "/apis/v1/"+item_type+"s/"+itemId;
   console.log("DELETE "+uri);
   $.ajax({
      type: "DELETE",
      url: uri,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
};

function deleteBookmark(bookmarkId)
{
   deleteItem("bookmark", bookmarkId);
};

function deletePhoto(photoId)
{
   deleteItem("photo", photoId);
};

function deleteContact(contactId)
{
   deleteItem("contact", contactId);
};

function deleteVideo(videoId)
{
   deleteItem("video", videoId);
};

function deleteSound(soundId)
{
   deleteItem("sound", soundId);
};

function deleteEvent(eventId)
{
   deleteItem("event", eventId);
};

function deleteLocation(locationId)
{
   deleteItem("location", locationId);
};

function deleteFileItem(fileitemId)
{
   deleteItem("fileitem", fileitemId);
};

function deleteBlog(blogId)
{
   deleteItem("blog", blogId);
};

function deleteComment(commentId)
{
   console.log("deleting comment: "+commentId);
   $.ajax({
      type: "DELETE",
      url: "/apis/v1/blogs/0/comments/"+commentId,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
};

function deleteUser(userId)
{
   deleteItem("admin/user", userId);
};

function deleteGroup(groupId)
{
   deleteItem("admin/group", groupId);
};

function deleteSession(sessionId)
{
   deleteItem("admin/session", sessionId);
};

function getItem(item_type, id)
{
   var retval;
   var uri = "/apis/v1/"+item_type+"s/"+id;
   console.log("GET "+uri);
   $.ajax({
      async: false,  // seems to be needed only for getTag(tagId);
      type: "GET",
      url: uri,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
}

function getBookmark(id)
{
   return getItem("bookmark", id);
}

function getPhoto(id)
{
   return getItem("photo", id);
}

function getContact(id)
{
   return getItem("contact", id);
}

function getVideo(id)
{
   return getItem("video", id);
}

function getSound(id)
{
   return getItem("sound", id);
}

function getEvent(id)
{
   return getItem("event", id);
}

function getLocation(id)
{
   return getItem("location", id);
}

function getFileItem(id)
{
   return getItem("fileitem", id);
}

function getBlog(id)
{
   return getItem("blog", id);
}

function getComment(id)
{
   return getItem("comment", id);
}

function search(pattern, page, pageSize)
{
   var retval = [];
   var uri = "/apis/v1/search";
   console.log("POST "+uri);
   $.ajax({
      async: false,
      type: "POST",
      url: uri,
      data: pattern,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
}

function getItems(item_type, page, pageSize, tagId)
{
   var retval = [];

   if (typeof tagId === "undefined") {
      tagId = -1;
   }
   var uri = "/apis/v1/"+item_type+"s?page="+page+"&pageSize="+pageSize
         +"&tagId="+tagId;
   console.log("GET "+uri);
   $.ajax({
      async: false,
      type: "GET",
      url: uri,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
}

function getPhotos(page, pageSize, tagId)
{
   return getItems("photo", page, pageSize, tagId);
}

function getContacts(page, pageSize, tagId)
{
   return getItems("contact", page, pageSize, tagId);
}

function getBookmarks(page, pageSize, tagId)
{
   return getItems("bookmark", page, pageSize, tagId);
}

function getEvents(page, pageSize, tagId)
{
   return getItems("event", page, pageSize, tagId);
}

function getLocations(page, pageSize, tagId)
{
   return getItems("location", page, pageSize, tagId);
}

function getVideos(page, pageSize, tagId)
{
   return getItems("video", page, pageSize, tagId);
}

function getSounds(page, pageSize, tagId)
{
   return getItems("sound", page, pageSize, tagId);
}

function getBlogs(page, pageSize, tagId)
{
   return getItems("blog", page, pageSize, tagId);
}

function getFileItems(page, pageSize, tagId)
{
   return getItems("fileitem", page, pageSize, tagId);
}

function getUsers(page, pageSize)
{
   return getItems("admin/user", page, pageSize);
}

function getGroups(page, pageSize)
{
   return getItems("admin/group", page, pageSize);
}

function getSessions(page, pageSize)
{
   return getItems("admin/session", page, pageSize);
}

function getComments(page, pageSize, blogId)
{
   var retval = [];
   var uri = "/apis/v1/blogs/";
   blogId = (typeof blogId === "undefined") ? -1 : blogId;
   uri = uri+blogId+"/comments?page="+page+"&pageSize="+pageSize;
   console.log("getting comments for blog "+blogId);
   console.log("GET "+uri);
   $.ajax({
      async: false,
      type: "GET",
      url: uri,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
}

function showBookmarks(tagId)
{
   var count = 0;
   $("#urls").html("");
   tagId = (typeof tagId === "undefined") ? -1 : tagId;
   $("#urls").append("<ul id=\"bookmarks\"><p>");
   jQuery.each(getBookmarks(tagId), function(idx, obj) {
      $("#urls").append("<li id=\""+this.id+"\">name: "+this.name+" ");
      $("#urls").append("url: "+this.url+" ");
      $("#urls").append("date: "+this.dateAsString+" ");
      $("#urls").append("rating: "+this.rating+"</li>");
      count++;
   })
   $("#urls").append("</ul><p>");
   console.log("showed "+count+" bookmarks");
};

function getTag(tagId)
{
   return getItem("tag", tagId);
}

function getTagsByName(tagName)
{
   var retval = [];

   if (typeof tagName === "undefined") {
      console.log("ERROR: cannot get tags for undefined name");
   }
   else {
      console.log("getting tags for name "+tagName);
      $.ajax({
         async: false,
         type: "GET",
         url: "/apis/v1/tags?tagName="+tagName,
         contentType: "application/json; charset=utf-8",
         dataType: "json",
         statusCode: {
            401: function (data) {
               console.log("basic auth required");
            }
         },
         success: function (data, status, jqXHR) {                  
            retval = data;
         },
         error: function (xhr) {
            // alert(xhr.responseText);
         }
      });
   }
   return retval;
}

function getTags(parentId)
{
   var retval = [];

   if (typeof parentId === "undefined") {
      console.log("ERROR: cannot get tags for undefined id");
   }
   else {
      console.log("getting tags for parent id "+parentId);
      console.log("GET /apis/v1/tags?parentId="+parentId);
      $.ajax({
         async: false,
         type: "GET",
         url: "/apis/v1/tags?parentId="+parentId,
         contentType: "application/json; charset=utf-8",
         dataType: "json",
         statusCode: {
            401: function (data) {
               console.log("basic auth required");
            }
         },
         success: function (data, status, jqXHR) {                  
            retval = data;
         },
         error: function (xhr) {
            // alert(xhr.responseText);
         }
      });
   }
   return retval;
}

function appendTags(branch, tags)
{
   for (var i=0; i<tags.length; i++) {
      var tag = tags[i];
      console.log("adding tag (id="+tag.id+" name="+tag.name+")");
      branch.addChild({
         title: tag.name,
         isFolder: true,
         isLazy: true,
         key: tag.id
      });
   };
   branch.setLazyNodeStatus(DTNodeStatus_Ok);
}

function getSession()
{
   var retval = [];
   $.ajax({
      async: false,
      type: "GET",
      url: "/apis/v1/security/details",
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      statusCode: {
         401: function (data) {
            console.log("basic auth required");
         }
      },
      success: function (data, status, jqXHR) {                  
         retval = data;
      },
      error: function (xhr) {
         // alert(xhr.responseText);
      }
   });
   return retval;
}
