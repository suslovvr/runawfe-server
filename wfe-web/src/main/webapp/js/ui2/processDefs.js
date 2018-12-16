wfe.processDefs = new function() {
    var app = null;

    this.onLoad = function() {
        this.onUnload();  // just in case
        wfe.ajaxGetJsonAndReady("processDefs", function(data) {
            app = new Vue({
                el: "#spa-body",
                data: data
            });
            var c_height = $(window).height()-180;
			$(".one-contentback").css('minHeight', c_height);
			$(".two-contentback").css('minHeight', c_height);
            $(".process-name").click(function() {
                $("#one-contentback").toggleClass("two-c30");
                $("#two-contentback").toggleClass("visibility-toggle");
                $(".long-description").hide();
                $(".firstform").show();
                $(".input-buttons").show();
            });
            $(".desc-ling-open").click(function() {
                $("#one-contentback").toggleClass("two-c30");
                $("#two-contentback").toggleClass("visibility-toggle");
                $(".long-description").show();
                $(".firstform").hide();
                $(".input-buttons").hide();
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
