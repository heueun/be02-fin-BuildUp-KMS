package com.example.bootshelf.boardsvc.board.controller;

import com.example.bootshelf.boardsvc.board.model.request.PatchUpdateBoardReq;
import com.example.bootshelf.boardsvc.board.model.request.PostCreateBoardReq;
import com.example.bootshelf.boardsvc.board.model.response.PostCreateBoardRes;
import com.example.bootshelf.boardsvc.board.service.BoardService;
import com.example.bootshelf.common.BaseRes;
import com.example.bootshelf.user.model.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Tag(name="Board", description = "Board 숙소 CRUD")
@Api(tags = "Board")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "Board 게시글 등록",
            description = "게시판에 게시글을 등록하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500",description = "서버 내부 오류")})
    @PostMapping("/create")
    public ResponseEntity<PostCreateBoardRes> createBoard(
            @AuthenticationPrincipal User user,
            @RequestPart(value="board") PostCreateBoardReq postCreateBoardReq,
            @RequestPart(value = "boardImage") MultipartFile[] uploadFiles
    ) {
        return ResponseEntity.ok().body(boardService.createBoard(user, postCreateBoardReq, uploadFiles));
    }

    @Operation(summary = "Board 본인 게시글 전체 조회",
            description = "본인이 작성한 게시글을 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
    @RequestMapping(method = RequestMethod.GET, value = "/mylist/{sortType}")
    public ResponseEntity<BaseRes> myList(
            @PageableDefault(size = 5) Pageable pageable,
            @PathVariable @NotNull(message = "조건 유형은 필수 입력 항목입니다.") @Positive(message = "조건 유형은 1이상의 양수입니다.") @ApiParam(value = "정렬유형 : 1 (최신순), 2 (추천수 순), 3 (조회수 순), 4 (스크랩수 순), 5 (댓글수 순)") Integer sortType
    ){
        User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        BaseRes baseRes = boardService.findMyBoardList(user, pageable, sortType);

        return ResponseEntity.ok().body(baseRes);
    }

    @Operation(summary = "Board 본인 게시글 카테고리별 조회",
            description = "본인이 작성한 게시글을 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
    @RequestMapping(method = RequestMethod.GET, value = "/mylist/{boardCategoryIdx}/{sortType}")
    public ResponseEntity<BaseRes> myListByCategory(
            @PageableDefault(size = 5) Pageable pageable,
            @PathVariable(value = "boardCategoryIdx") Integer boardCategoryIdx,
            @PathVariable @NotNull(message = "조건 유형은 필수 입력 항목입니다.") @Positive(message = "조건 유형은 1이상의 양수입니다.") @ApiParam(value = "정렬유형 : 1 (최신순), 2 (추천수 순), 3 (조회수 순), 4 (스크랩수 순), 5 (댓글수 순)") Integer sortType
    ){
        User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        BaseRes baseRes = boardService.findMyBoardListByCategory(user, pageable, boardCategoryIdx, sortType);

        return ResponseEntity.ok().body(baseRes);
    }

    @Operation(summary = "Board 게시글 카테고리별 조회",
            description = "게시판의 게시글을 카테고리별로 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
    @RequestMapping(method = RequestMethod.GET, value = "/list/{boardCategoryIdx}/{sortType}")
    public ResponseEntity<BaseRes> boardListbyCategory(
            @PageableDefault(size = 5) Pageable pageable,
            @PathVariable(value = "boardCategoryIdx") Integer boardCategoryIdx,
            @PathVariable @NotNull(message = "조건 유형은 필수 입력 항목입니다.") @Positive(message = "조건 유형은 1이상의 양수입니다.") @ApiParam(value = "정렬유형 : 1 (최신순), 2 (추천수 순), 3 (조회수 순), 4 (스크랩수 순), 5 (댓글수 순)") Integer sortType
    ){
        BaseRes baseRes = boardService.findListByCategory(pageable, boardCategoryIdx, sortType);

        return ResponseEntity.ok().body(baseRes);
    }

    @Operation(summary = "Board 게시글 상세 조회",
            description = "게시판의 게시글을 게시글의 idx로 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500",description = "서버 내부 오류")})
    @GetMapping ("/{boardIdx}")
    public ResponseEntity<BaseRes> findBoard (
            @PathVariable Integer boardIdx
    ){
        return ResponseEntity.ok().body(boardService.listBoard(boardIdx));
    }


    @Operation(summary = "Board 게시글 검색어로 조회",
            description = "게시판의 게시글을 검색어(키워드)로 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500",description = "서버 내부 오류")})
    @GetMapping ("/search")
    public ResponseEntity<BaseRes> searchBoardListByQuery (
            @RequestParam String query,
            @RequestParam Integer searchType,
            @PageableDefault(size = 20) Pageable pageable

    ){
        return ResponseEntity.ok().body(boardService.searchBoardListByQuery(query, searchType, pageable));
    }


    @Operation(summary = "Board 게시글 수정 기능",
            description = "게시판의 게시글을 수정하는 API 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
    @RequestMapping(method = RequestMethod.PATCH, value = "/update/{boardIdx}")
    public ResponseEntity<BaseRes> updateBoard(
            @Valid @RequestBody PatchUpdateBoardReq patchUpdateBoardReq,
            @PathVariable(value = "boardIdx") Integer boardIdx
    ){
        User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        BaseRes baseRes = boardService.updateBoard(user, patchUpdateBoardReq, boardIdx);
        return ResponseEntity.ok().body(baseRes);
    }

    @Operation(summary = "Board 게시글 삭제 기능",
        description = "게시판의 게시글을 삭제하는 API 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
    @RequestMapping(method = RequestMethod.DELETE, value = "/delete/{boardIdx}")
    public ResponseEntity<BaseRes> deleteBoard(
            @PathVariable(value = "boardIdx") Integer boardIdx
    ){
        User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        BaseRes baseRes = boardService.deleteBoard(user, boardIdx);

        return ResponseEntity.ok().body(baseRes);
    }
}
