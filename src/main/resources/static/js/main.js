jQuery(document).ready(function($){

	if (!Modernizr.svg) {
			$("img[src$='.svg']").each(function(){
				$(this).attr("src",$(this).attr("src").replace("svg","png"));
			})
		}
	
	$menuvisible = false;

	$(window).on("resize",function(){
		if($menuvisible && $(this).width() > 1050){
			hideMenu();
			$menuvisible = false;
		}
	})

	function hideMenu(){
			$(".main-menu-button").html("menu");
			$("#page").animate({
				"marginLeft":0
			},500,function(){
				$("#page").css({"position":"relative","width":"auto"});
			})
			$("#responsive-menu").animate({
				"right":-360
			},500)
			$menuvisible = false;
	}

	function showMenu(){
			$(".main-menu-button").html("hide menu");
			$("#page").css({"position":"absolute","width":"100%"});
			$("#page").animate({
				"marginLeft":-360
			},500)
			$("#responsive-menu").animate({
				"right":0
			},500)
			$menuvisible = true;
	}

	$(".menu-button").click(function(e){
		if(!$menuvisible){
			showMenu();
		}
		else{
			hideMenu();
		}
		e.preventDefault();
	})
})