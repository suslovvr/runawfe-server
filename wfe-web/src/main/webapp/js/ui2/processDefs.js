wfe.processDefs = new function() {
    var app = null;

    this.onLoad = function() {
        this.onUnload();  // just in case
        wfe.ajaxGetJsonAndReady("processDefs", function(data) {
            app = new Vue({
                el: "#spa-body",
                data: data
            });
        });
    };

    this.onUnload = function() {
        if (app) {
            app.$destroy();
            app = null;
        }
    };
};
