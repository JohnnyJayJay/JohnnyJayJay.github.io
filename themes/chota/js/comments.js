/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * SPDX-FileCopyrightText: 2023 Carl Schwan
 * SPDX-FileCopyrightText: 2023 JohnnyJayJay
*/

// initial state of the comment ui
const commentUi = document.getElementById("comment-ui");
const instance = commentUi.dataset.postInstance;
const author = commentUi.dataset.postAuthor;
const id = commentUi.dataset.postId;

function addUrlCopyButton() {
  // Enhance Fediverse URL copying if JavaScript is available
  const button = document.createElement("button");
  button.className = "col-1 is-center primary button text-lg";

  const icon = document.createElement("img");
  icon.src = document.body.dataset.sitePrefix + "/icons/copy.svg";
  icon.alt = "Copy icon";
  icon.width = "24";

  button.appendChild(icon);
  document.getElementById("comment-form").appendChild(button);

  const fediUrl = document.getElementById("fedi-url-field").value;

  button.addEventListener("click", function() {
    event.preventDefault();
    navigator.clipboard.writeText(fediUrl);

    icon.src = document.body.dataset.sitePrefix + "/icons/check.svg";
    icon.alt = "Checkmark icon";
    setTimeout(function() {
      icon.src = document.body.dataset.sitePrefix + "/icons/copy.svg";
      icon.alt = "Copy icon";
    }, 1500);
  });
}

addUrlCopyButton();

// scroll dropdown into view when opened
const details = document.getElementById("comment-button")
details.addEventListener("toggle", function() {
  if (details.hasAttribute("open")) {
    details.scrollIntoView({"behavior": "smooth", "block": "center"});
  }
});

// Add button to load comments
const loadButton = document.createElement("button");
loadButton.textContent = "Load comments";
commentUi.appendChild(loadButton);

document.getElementById("comment-load-info").innerHTML
  = `You can also view the comments embedded right here by clicking the load button.
Note that this <strong>will</strong> make a request to <a href="https://${instance}">${instance}</a>.`;


const dateOptions = {
  year: "numeric",
  month: "numeric",
  day: "numeric",
  hour: "numeric",
  minute: "numeric",
};

function escapeHtml(unsafe) {
  return unsafe
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// comment loading logic
loadButton.addEventListener("click", function() {
  loadButton.textContent = "Loading...";
  fetch(`https://${instance}/api/v1/statuses/${id}/context`)
    .then(function(response) {
      return response.json();
    })
    .then(function(data) {
      if(data['descendants'] &&
         Array.isArray(data['descendants']) &&
         data['descendants'].length > 0) {
        // replace loading button with comment list inside collapsible details
        loadButton.outerHTML = `<details id="comment-details" open="true">
                     <summary id="comment-scroll-target">Comments</summary>
                     <ul id="mastodon-comments-list"><li></li></ul>
                 </details>`;
        const commentList = document.getElementById('mastodon-comments-list');
        data['descendants'].forEach(function(reply) {
          // todo: add some sort of check mark to comments from OP
          reply.account.display_name = escapeHtml(reply.account.display_name);
          reply.account.reply_class = reply.in_reply_to_id == id ? "reply-original" : "reply-child";
          reply.created_date = new Date(reply.created_at);
          reply.account.emojis.forEach(emoji => {
            reply.account.display_name = reply.account.display_name.replace(`:${emoji.shortcode}:`,
                                                                            `<img src="${escapeHtml(emoji.static_url)}" alt="Emoji ${emoji.shortcode}" height="20" width="20" />`);
          });
          const attCount = reply.media_attachments.length;
          const attNotice = attCount > 0 ? `<p><a href="${reply.url}">${attCount} attachment${attCount > 1 ? "s" : ""}</a></p>` : '';
          mastodonComment =
            `
<li class="mastodon-wrapper card">
  <div class="comment-level ${reply.account.reply_class}"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
    <path fill="currentColor" stroke="currentColor" d="m 307,477.17986 c -11.5,-5.1 -19,-16.6 -19,-29.2 v -64 H 176 C 78.8,383.97986 -4.6936293e-8,305.17986 -4.6936293e-8,207.97986 -4.6936293e-8,94.679854 81.5,44.079854 100.2,33.879854 c 2.5,-1.4 5.3,-1.9 8.1,-1.9 10.9,0 19.7,8.9 19.7,19.7 0,7.5 -4.3,14.4 -9.8,19.5 -9.4,8.8 -22.2,26.4 -22.2,56.700006 0,53 43,96 96,96 h 96 v -64 c 0,-12.6 7.4,-24.1 19,-29.2 11.6,-5.1 25,-3 34.4,5.4 l 160,144 c 6.7,6.2 10.6,14.8 10.6,23.9 0,9.1 -3.9,17.7 -10.6,23.8 l -160,144 c -9.4,8.5 -22.9,10.6 -34.4,5.4 z" />
  </svg></div>
  <div class="mastodon-comment">
    <div class="comment">
      <div class="comment-avatar"><img src="${escapeHtml(reply.account.avatar_static)}" alt=""></div>
      <div class="comment-author">
        <div class="comment-author-name"><a href="${reply.account.url}" rel="nofollow">${reply.account.display_name}</a></div>
        <div class="comment-author-reply"><a href="${reply.account.url}" rel="nofollow">${escapeHtml(reply.account.acct)}</a>${reply.account.acct == author ? ' [OP]' : ""}</div>
      </div>
      <time datetime="${reply.created_date.toISOString()}" class="comment-author-date"><a href="${reply.url}" rel="nofollow">${reply.created_date.toLocaleString(navigator.language, dateOptions)}</a></time>
    </div>
    <div class="comment-content">${reply.content}${attNotice}</div>
  </div>
</li>
                                         `;
          commentList.appendChild(DOMPurify.sanitize(mastodonComment, {'RETURN_DOM_FRAGMENT': true}));
        });

        // smooth scrolling of comments into view
        const commentDetails = document.getElementById("comment-details");
        const scrollTarget = document.getElementById("comment-scroll-target");
        scrollTarget.scrollIntoView({"block": "center", "behavior": "smooth"});
        commentDetails.addEventListener("toggle", function() {
          if (commentDetails.hasAttribute("open")) {
            scrollTarget.scrollIntoView({"block": "center", "behavior": "smooth"});
          }
        })

      } else {
        loadButton.outerHTML = "<p><b>No comments (yet!)</b></p>";
      }
    });
});
