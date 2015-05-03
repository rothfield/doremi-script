Zepto(function($){
		$('.show_ly').on('click', 
				function(e) {
						$('#ly' + $(this).data('which')).toggle();
				});
		$('.show_json').on('click', 
				function(e) {
						$('#json' + $(this).data('which')).toggle();
				});
});
