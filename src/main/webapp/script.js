function chkForm() {
	var f = document.frm; //form태그
	
	if(f.title.value == '') {
		alert("제목을 입력해주십시오.");
		return false;
	}
	
	if(f.user_id.value == '') {
		alert("아이디를 입력해주십시오.");
		return false;
	}
	
	f.submit(); //폼태그 전송
}

function chkDelete(board_no) {
	const result = confirm("삭제하시겠습니까?");
	
	if(result) {
		const url = location.origin;
		location.href = url + "/board2/delete?board_no=" + board_no;
	} else {
		return false;
	}		
}
