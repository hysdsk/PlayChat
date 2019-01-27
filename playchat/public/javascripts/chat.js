var webSocket = null

// 初期処理登録
$(function() {
	// 初期化
	$('#entry, #speech, #send, #leave').prop('disabled', true)
	
	// 名前 キーアップイベント
	$('#user').keyup(function(){
		$('#entry').prop('disabled', !($(this).val().length > 0))
	})
	
	// 入場ボタン クリックイベント
	$('#entry').click(function(){
		var roomId = $('#room').val()
		webSocket = new WebSocket(`ws://127.0.0.1:9000/chat/${roomId}`)
	    // イベントハンドラの設定
	    webSocket.onopen = function(event){
			console.log("接続しました。")
	    }
	  
	    webSocket.onmessage = function(event){
	    	var data = JSON.parse(event.data)
	    	var text, align
	    	if($('#user').val() == data.user){
	    		text = `${data.user} => ${data.text}`
	    		align = 'left'
	    	}else{
	    		text = `${data.text} <= ${data.user}`
	    		align = 'right'
	    	}
	    	
	    	var div = $('<div>').text(text)
    		$('div.talk').prepend($(div).css('text-align', align))
	    	
	    }
	  
	    webSocket.onclose = function(event){
	    	console.log("接続を切断しました。")
	    	
	    	$('div.talk').children().remove()
		
			$('#user, #room, #entry').prop('disabled', false)
		    $('#speech, #send, #leave').prop('disabled', true)
		    $('#speech').val("")
	    }
	  
	    webSocket.onerror = function(event){
	  	    console.log("エラーが発生しました。")
	    }
	  
	    $('#user, #room, #entry').prop('disabled', true)
	    $('#speech, #leave').prop('disabled', false)
	})
	
	// 発言 キーアップイベント
	$('#speech').keyup(function(){
		$('#send').prop('disabled', !($(this).val().length > 0))
	})

	// 送信 クリックイベント
	$('#send').click(function(){
	    var user = $('#user').val()
	    var text = $('#speech').val()

	    if (webSocket) {
	    	webSocket.send(JSON.stringify({user: user ,text: text, systemFlag: false}))
	    	$('#speech').val("")
	    	$('#send').prop('disabled', true)
	    }
	})
	
	// 退出 クリックイベント
	$('#leave').click(function(){
		webSocket.close()
		webSocket = null
	})
	
})
