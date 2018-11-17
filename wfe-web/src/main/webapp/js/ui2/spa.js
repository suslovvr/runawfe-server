// TODO div#spa-splash, div#spa-wait, ajax error handling, localization, hash in hash (e.g. for tab controls; don't reload when 2nd hash changes).

function spaGotoUrl(s) {
    window.location.hash = "#" + s;
}

function spaInit(versionHash) {
    var title0 = document.title;
    var attachedScriptsAndStyles = {};
    var cachedHtmls = {};

    function showPage(html) {
        // Prevent loss of <html>, <head>, <body> tags; https://stackoverflow.com/a/10585079/4247442
        var eHtml = document.createElement("html");
        eHtml.innerHTML = html;
        var qHtml = $(eHtml);

        var title = qHtml.find("head title").text();
        document.title = title ? title + " - " + title0 : title0;

        // Inline scripts & styles are forbidden; see "onload" below.
        qHtml.find("head script[src], head style[href]").each(function () {
            var q = $(this);
            var attrName = this.tagName == 'SCRIPT' ? 'src' : 'href';
            var fileUri = q.attr(attrName);
            if (!attachedScriptsAndStyles[fileUri]) {
                attachedScriptsAndStyles[fileUri] = true;
                $('head').append(q.clone().attr(attrName, fileUri + "?" + versionHash));
            }
        });

        var qBody = qHtml.find("body");
        $("#spa-body").empty().append(qBody.contents().clone());

        // *.js files are cached, inline scripts are ignored. So need custom bootstrap stuff.
        var onload = qBody.attr('onload');
        if (onload) {
            // Instead of onload() for each <script>; https://stackoverflow.com/a/14786759/4247442
            setTimeout(onload, 10);
        }
    }

    $(window).on("hashchange", function () {
        var hash = window.location.hash;
        if (!hash.startsWith("#/")) {
            spaGotoUrl("/tasks");
            return;
        }

        var url = hash.substr(1);
        if (url.endsWith("/")) {
            url += "index";
        }
        url = "/wfe/html/ui2" + url + ".html?" + versionHash;

        if (cachedHtmls[url]) {
            showPage(cachedHtmls[url]);
        } else {
            $.ajax({
                url: url,
                dataType: "html",
                success: function (html) {
                    cachedHtmls[url] = html;
                    showPage(html);
                }
            });
        }
    });

    spaGotoUrl("/tasks");
}
