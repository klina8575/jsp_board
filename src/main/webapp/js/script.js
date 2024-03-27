//게시물 등록 전 체크
function chkForm() {
	var f = document.frm; //form 태그 요소
	
	if(f.title.value == '') {
		alert("제목을 입력해주세요.");
		return false; //함수 종료시킴(걸리면 뒤에 있는 코드 실행할 필요X)
	}	
	
	if(f.user_id.value == '') {
		alert("글쓴이를 입력해주세요.");
		return false;  //함수 종료시킴
	}	
	
	if(f.content.value == '') {
		alert("내용을 입력해주세요.");
		return false;  //함수 종료시킴
	}	
	
	f.submit(); //서버로 폼태그 안의 데이터 전송
}

function chkDelete(board_no) {
	const result = confirm("삭제하시겠습니까?");
	
	if(result) {
		const url = location.origin;
		
		//페이지 이동(request)
		location.href = url + "/jsp_board/delete?board_no=" + board_no;
	} else {
		return false;
	}
}


