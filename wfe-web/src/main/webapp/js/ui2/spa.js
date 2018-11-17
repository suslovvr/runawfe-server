// TODO Error page (#spa-error): main menu, links "Retry" & "Go home".
// TODO Localization.
// TODO IE.
// TODO Wiki, incl. restrictions on HTML pages and JS files; cumulative JS & CSS includes.

var wfeSpa = new function() {
    var self = this;
    var versionHash;
    var defaultUrl = "/tasks";
    var titleSuffix = document.title;
    var attachedScriptsAndStyles = {};
    var cachedHtmls = {};

    this.wait = function() {
        $('#spa-wait').show();
    };
    this.ready = function() {
        $('#spa-body').show();
        $('#spa-error').hide();
        $('#spa-wait').hide();
    };
    this.error = function() {
        $('#spa-body').hide();
        $('#spa-error').show();
        $('#spa-wait').hide();
    };

    function showPage(html, fromCache) {
        // Prevent loss of <html>, <head>, <body> tags; https://stackoverflow.com/a/10585079/4247442
        var eHtml = document.createElement("html");
        eHtml.innerHTML = html;
        var qHtml = $(eHtml);

        var title = qHtml.find("head title").text();
        document.title = title ? title + " - " + titleSuffix : titleSuffix;

        var qBody = qHtml.find("body");
        $("#spa-body").empty().append(qBody.contents().clone());

        // HTML can include JS files in <head> and have body/@onload attribute; no other JS is allowed in HTML.
        function onPageLoaded() {
            var onload = qBody.attr('onload');
            if (onload) {
                // It must call wfeSpa.ready() itself.
                eval(onload);
            } else {
                self.ready();
            }
        }

        // Page must not and cannot call gotoPage() before it's fully loaded.
        // So if we're loading from cache, all JS and CSS files must already be loaded.
        var numFilesToWaitFor = 0;
        if (!fromCache) {
            function onFileLoaded() {
                if (--numFilesToWaitFor <= 0) {
                    onPageLoaded();
                }
            }

            // Inline scripts & styles are ignored; JS files cannot contain $(function) since they are loaded only once.
            // So see onPageLoaded() above.
            qHtml.find("head script[type='text/javascript'][src], head link[rel='stylesheet'][type='text/css'][href]").each(function () {
                var isScript = this.tagName === 'SCRIPT';
                var attrName = isScript ? 'src' : 'href';
                var q = $(this);
                var fileUrl = q.attr(attrName);
                if (!attachedScriptsAndStyles[fileUrl]) {
                    attachedScriptsAndStyles[fileUrl] = true;
                    numFilesToWaitFor++;

                    // Does not work with jquery; https://stackoverflow.com/a/22534608/4247442, https://stackoverflow.com/a/11425185/4247442
                    var e = document.createElement(this.tagName);
                    e.onload = onFileLoaded;
                    if (isScript) {
                        e.setAttribute('type', 'text/javascript');
                        e.setAttribute('src', fileUrl);
                    } else {
                        e.setAttribute('rel', 'stylesheet');
                        e.setAttribute('type', 'text/css');
                        e.setAttribute('href', fileUrl);
                    }
                    document.head.appendChild(e);
                }
            });
        }
        if (numFilesToWaitFor === 0) {
            onPageLoaded();
        }
    }

    function onHashChange() {
        var hash = window.location.hash;
        if (!hash.startsWith("#/")) {
            self.gotoUrl(defaultUrl);
            return;
        }

        var url = hash.substr(1);
        if (url.endsWith("/")) {
            url += "index";
        }
        url = "/wfe/html/ui2" + url + ".html?" + versionHash;

        if (cachedHtmls[url]) {
            showPage(cachedHtmls[url], true);
        } else {
            self.wait();
            $.ajax({
                url: url,
                dataType: "html",
                success: function (html) {
                    cachedHtmls[url] = html;
                    showPage(html, false);
                },
                error: function() {
                    self.error();
                }
            });
        }
    }

    this.gotoUrl = function(s) {
        s = "#" + s;
        if (window.location.hash === s) {
            // Reload current page (otherwise F5 & Ctrl+F5 won't work, also we may need to reload programmatically).
            onHashChange();
        } else {
            window.location.hash = s;
        }
    };

    this.init = function(versionHash_) {
        versionHash = versionHash_;
        $(window).on("hashchange", onHashChange);
        wfeSpa.gotoUrl(defaultUrl);
    }
};
