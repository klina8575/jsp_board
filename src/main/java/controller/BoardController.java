package controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.beanutils.BeanUtils;

import DAO.BoardDAO;
import DTO.Board;

//server.xml에 추가 <Context docBase="c:/Temp/img" path="/img" reloadable="true"/>

@WebServlet("/")
@MultipartConfig(maxFileSize=1024*1024*2, location="c:/Temp/img")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BoardDAO dao;
	private ServletContext ctx;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		// init은 서블릿 객체 생성시 딱 한번만 실행하므로 객체를 한번만 생성해 공유할 수 있다.
		dao = new BoardDAO(); 
		ctx = getServletContext(); //ServletContext: 웹 어플리케이션의 자원 관리
	}
	
	

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String context = request.getContextPath();
		request.setCharacterEncoding("UTF-8"); //한글깨짐 방지
		
		String command = request.getServletPath();
		String site = null;
		//System.out.println(site);
		
		//경로 라우팅(경로를 찾아줌)
		switch (command) {
		case "/list":
			site = getList(request);
			break;
		case "/view":
			site = getView(request);
			break;
		case "/write": //글쓰기 화면을 보여줌
			site = "write.jsp";
			break;	
		case "/insert": //insert 기능
			site = insertBoard(request);
			break;
		case "/edit": //수정 화면을 보여줌
			site = getViewForEdit(request);
			break;
		case "/update": //update 기능
			site = updateBoard(request);
			break;
		case "/delete": //delete 기능
			site = deleteBoard(request);
			break;
		}
		
		
		/*
		 * -둘다 페이지를 이동한다.
		   redirect: URL의 변화 O, 객체의 재사용 X (request, response객체)
		      *DB에 변화가 생기는 요청에 사용(글쓰기, 회원가입...) insert, update, delete조심...
		   
		   forward: URL의 변화 X(보안..), 객체의 재사용 O (request, response객체)
		      *단순 조회(리스트보기, 검색) select..
		 */
		
		if(site.startsWith("redirect:/")) { //redirect
			String rview = site.substring("redirect:/".length());
			//System.out.println(rview);
			response.sendRedirect(rview);
		} else { //forward
			ctx.getRequestDispatcher("/" + site).forward(request, response);
		}
	}



	/*
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); //request 객체에 저장된 한글 깨짐 방지
		doPro(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); //request 객체에 저장된 한글 깨짐 방지
		doPro(request, response);
	}
	*/

	/*
	protected void doPro(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String context = request.getContextPath();
		String command = request.getServletPath();
		String site = null;
		System.out.println(site);
		
		//경로 라우팅(경로를 찾아줌)
		switch (command) {
		case "/list":
			site = getList(request);
			break;
		case "/view":
			site = getView(request);
			break;
		case "/write": //글쓰기 화면을 보여줌
			site = "write.jsp";
			break;	
		case "/insert": //insert 기능
			site = insertBoard(request);
			break;
		case "/edit": //수정 화면을 보여줌
			site = getViewForEdit(request);
			break;
		case "/update": //update 기능
			site = updateBoard(request);
			break;
		case "/delete": //delete 기능
			site = deleteBoard(request);
			break;
		}
		


		
		if(site.startsWith("redirect:/")) { //redirect
			String rview = site.substring("redirect:/".length());
			System.out.println(rview);
			response.sendRedirect(rview);
		} else { //forward
			ctx.getRequestDispatcher("/" + site).forward(request, response);
		}
	}

    */
	
	public String getList(HttpServletRequest request) {
		List<Board> list;
		
		try {
			list = dao.getList();
			request.setAttribute("boardList", list);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시판 목록 생성 과정에서 문제 발생");
			//나중에 사용자 한테 에러메세지를 보여주기 위해 저장
			request.setAttribute("error", "게시판 목록이 정상적으로 처리되지 않았습니다!"); 
		}
		
		return "index.jsp";
	}
	
	public String getView(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		
		try {
			dao.updateViews(board_no); //조회수 증가
			Board b = dao.getView(board_no);
			request.setAttribute("board", b);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 가져오는 과정에서 문제 발생");
			request.setAttribute("error", "게시글을 정상적으로 처리되지 않았습니다!"); 
		}
		
		return "view.jsp";
	}
	
	public String insertBoard(HttpServletRequest request) {
		Board b = new Board();
		
		/*
		b.setUser_id(request.getParameter("user_id"));
		b.setTitle(request.getParameter("title"));
		b.setContent(request.getParameter("content")); 
	    */
		
		
		try {
			BeanUtils.populate(b, request.getParameterMap());

			// 이미지 파일 저장
			Part part = request.getPart("file");
			String fileName = getFilename(part);
			if(fileName != null && !fileName.isEmpty()){
				part.write(fileName);
			}	        
			
			// 이미지 파일 이름을 News 객체에도 저장
	        b.setImg("/img/"+fileName);
	        
			dao.insertBoard(b);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("추가 과정에서 문제 발생");
			try {
				//get방식으로 넘겨줄때 한글 깨짐을 방지한다.
				String encodeName = URLEncoder.encode("게시물이 정상적으로 등록되지 않았습니다!", "UTF-8");
				return "redirect:/list?error=" + encodeName;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		} 
		
        return "redirect:/list";
//		return "/list";
	}
	
	public String getViewForEdit(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		
		try {
			Board b = dao.getViewForEdit(board_no);
			request.setAttribute("board", b);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 가져오는 과정에서 문제 발생");
			request.setAttribute("error", "게시글을 정상적으로 처리되지 않았습니다!"); 
		}
		
		return "edit.jsp";
	}
	
	public String updateBoard(HttpServletRequest request) {
		Board b = new Board();
		try {
			BeanUtils.populate(b, request.getParameterMap());

			// 이미지 파일 저장
			Part part = request.getPart("file");
			String fileName = getFilename(part);
			if(fileName != null && !fileName.isEmpty()){
				part.write(fileName);
			}	        
			
			
			// 이미지 파일 이름을 News 객체에도 저장
	        b.setImg("/img/"+fileName);
	
			dao.updateBoard(b);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("수정 과정에서 문제 발생");
			try {
				//get방식으로 넘겨줄때 한글 깨짐을 방지한다.
				String encodeName = URLEncoder.encode("게시물이 정상적으로 수정되지 않았습니다!", "UTF-8");
				return "redirect:/view?board_no=" + b.getBoard_no() + "&error=" + encodeName;				
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		return "redirect:/view?board_no=" + b.getBoard_no();
	}
	
	public String deleteBoard(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		
		try {
			dao.deleteBoard(board_no);
		} catch(Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 삭제하는 과정에서 문제 발생");
			String encodeName;
			try {
				encodeName = URLEncoder.encode("게시물이 정상적으로 삭제되지 않았습니다!", "UTF-8");
				return "redirect:/list?error=" + encodeName;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		return "redirect:/list";
	}
	
    // multipart 헤더에서 파일이름 추출
	private String getFilename(Part part) {
        String fileName = null;   
        // 파일이름이 들어있는 헤더 영역을 가지고 옴
        String header = part.getHeader("content-disposition");
        //part.getHeader -> form-data; name="img"; filename="사진5.jpg"
        System.out.println("Header => "+header);

        // 파일 이름이 들어있는 속성 부분의 시작위치를 가져와 쌍따옴표 사이의 값 부분만 가지고옴
        int start = header.indexOf("filename=");
        fileName = header.substring(start+10,header.length()-1);        
        ctx.log("파일명:"+fileName);        
        return fileName; 
	}
}
