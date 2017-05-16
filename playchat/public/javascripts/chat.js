var webSocket = null

// 初期処理登録
$(function() {
	
	$('#send').prop('disabled', true)
	$('#leave').prop('disabled', true)
	$('#speech').prop('disabled', true)
	
	$('#entry').click(function(){
		var roomId = $('#room').val()
		webSocket = new WebSocket(`ws://127.0.0.1:9000/chat/${roomId}`)
	    // イベントハンドラの設定
	    webSocket.onopen = function(event){
			console.log("接続しました。")
	    }
	  
	    webSocket.onmessage = function(event){
	    	var data = JSON.parse(event.data)
	    	if($('#user').val() == data.user){
	    		var div = $('<div>').text(`${data.user} => ${data.text}`)
	    		$(div).css('text-align', 'left')
	    		$('div.talk').prepend(div)
	    	}else{
	    		var div = $('<div>').text(`${data.text} <= ${data.user}`)
	    		$(div).css('text-align', 'right')
	    		$('div.talk').prepend(div)
	    	}
	    	
	    }
	  
	    webSocket.onclose = function(event){
	    	console.log("接続を切断しました。")
	    }
	  
	    webSocket.onerror = function(event){
	  	    console.log("エラーが発生しました。")
	    }
	  
	    $('#user').prop('disabled', true)
	    $('#room').prop('disabled', true)
	    $('#entry').prop('disabled', true)
	    $('#leave').prop('disabled', false)
	    $('#speech').prop('disabled', false)
	})

	$('#send').click(function(){
	    var user = $('#user').val()
	    var text = $('#speech').val()

	    if (webSocket) {
	    	webSocket.send(JSON.stringify({user: user ,text: text, systemFlag: false}))
	    	$('#speech').val("")
	    	$('#send').prop('disabled', true)
	    }
	})
	
	$('#leave').click(function(){
		webSocket.close()
		webSocket = null
		$('div.talk').children().remove()
		
		$('#user').prop('disabled', false)
	    $('#room').prop('disabled', false)
	    $('#entry').prop('disabled', false)
	    $('#speech').prop('disabled', true).val("")
	    $('#send').prop('disabled', true)
	    $('#leave').prop('disabled', true)
	    
	})
	
	$('#speech').keyup(function(){
		if($(this).val().length > 0){
			$('#send').prop('disabled', false)
		}else{
			$('#send').prop('disabled', true)
		}
	})
	
})
