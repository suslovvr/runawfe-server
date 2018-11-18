wfe.myTasks = new function() {

    this.onLoad = function() {
        wfe.ajaxGetJson("getMyTasks", function(data) {
            // TODO Need onUnload() to make Vue a class member.
            var app = new Vue({
                el: ".content",
                data: data
            });
            app.$destroy();
        });
    }
};
