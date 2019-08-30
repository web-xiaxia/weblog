<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>WebSocket Logger</title>
    <script src="logjs/jquery.min.js"></script>
    <script src="logjs/sockjs.min.js"></script>
    <script src="logjs/stomp.min.js"></script>
    <style type="text/css">
        #log-name-list {
            padding: 0px;
            list-style: none;
        }

        #log-name-list > li {
            display: inline-block;
            padding: 0px 20px;
            cursor: pointer;
        }

        .fffb {
            position: fixed;
            display: block;
            width: 100%;
            background-color: white;
        }
    </style>
</head>
<body>
<div>
    <ul id="log-name-list">

    </ul>
    <div id="show-main"></div>

</div>
</body>
<script>
    function generateUUID() {
        var d = new Date().getTime();
        if (window.performance && typeof window.performance.now === "function") {
            d += performance.now(); //use high-precision timer if available
        }
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
        return uuid;
    }

    var clientId = generateUUID();
    var session = {
        weblogClientId: clientId
    };

    var stompClient = Stomp.over(new SockJS('${weblogConfig.endpoint}'));
    stompClient.debug = function () {
    }

    var subscribes = [];
    var showcroll = [];

    $(document).ready(function () {
        stompClient.connect(session, connectCallback);
    });

    function stopScroll(index) {
        showcroll[index] = false
    }

    function startScroll(index) {
        var nowDiv = $(".show-log-div").eq(index);
        nowDiv.scrollTop(nowDiv[0].scrollHeight);
        showcroll[index] = true
    }

    function qin(index) {
        var nowDiv = $(".show-log-div").eq(index);
        nowDiv.find("div").eq(1).html("");
    }

    function need(index) {
        qin(index);
        stompClient.send("${weblogConfig.applicationDestinationPrefix}/weblog/need", session, JSON.stringify({
            weblogClientId: clientId,
            index: index
        }));
    }

    function connectCallback(frame) {
        stompClient.subscribe('${weblogConfig.userDestinationPrefix}/' + clientId + '/init', function (event) {
            //console.log(event);
            if (showcroll.length) {
                return;
            }

            var nameList = $("#log-name-list")
            var showMain = $("#show-main")
            JSON.parse(event.body)
                .forEach((v, i) => {
                    nameList.append("<li onclick='showLog(" + i + ")'>" + v.name + "</li>");
                    showMain.append("<div class='show-log-div' style='display: none ; height: 800px; overflow: scroll; position: relative;'>" +
                        "<div class='fffb'> <button onclick='startScroll(" + i + ")'>继续滚动</button> <button onclick='stopScroll(" + i + ")'>暂停滚动</button> <button onclick='need(" + i + ")'>最近50000</button> <button onclick='qin(" + i + ")'>清屏</button> </div>" +
                        "<div></div>" +
                        "</div>");
                    showcroll[i] = true;
                    stompClient.subscribe('${weblogConfig.topicPrefix}/weblog/pullFileLogger/' + i, function (event) {
                        var content = event.body;
                        var nowDiv = $(".show-log-div").eq(i);
                        nowDiv.find("div").eq(1).append(content).append("<br/>");
                        if (showcroll[i]) {
                            nowDiv.scrollTop(nowDiv[0].scrollHeight);
                        }
                    })
                })
            $(".show-log-div").eq(0).css({"display": "block"});
        });
        /*stompClient.subscribe('/com.xiaxia.weblog.core.weblog/user/init', function (event) {
            console.log(event);
        });*/
        stompClient.send("${weblogConfig.applicationDestinationPrefix}/weblog/init", session, JSON.stringify(session));

        //stompClient.send("/queue", {}, JSON.stringify({ 'name': name}));
        //console.log(frame)
        /*var xx1=stompClient.subscribe('/topic/pullLogger', function(event) {
            var content=JSON.parse(event.body);
            $("#log-container div").append(content.timestamp +" "+ content.level+" --- ["+ content.threadName+"] "+ content.className+"   :"+content.body).append("<br/>");
            $("#log-container").scrollTop($("#log-container div").height() - $("#log-container").height());
        });

        var xx2=stompClient.subscribe('/topic/pullFileLogger/0', function(event) {
            var content=event.body;
            $("#filelog-container div").append(content).append("<br/>");
            $("#filelog-container").scrollTop($("#filelog-container div").height() - $("#filelog-container").height());
        });


        subscribes.push(xx2)*/

    }

    function showLog(index) {
        $(".show-log-div").css({"display": "none"})
        var nowDiv = $(".show-log-div").eq(index);
        nowDiv.css({"display": "block"})

    }

</script>
</body>
</html>