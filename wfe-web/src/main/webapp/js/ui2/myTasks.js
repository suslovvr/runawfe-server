wfe.myTasks = new function() {

    this.onLoad = function() {
        wfe.ajaxGetJsonAndReady("myTasks", function(data) {
            var app = new Vue({
                el: "#spa-body",
                data: data
            });
            // TODO Need onUnload() to make Vue a JS class member instead of destroying it instantly.
            app.$destroy();
        });
    }
};