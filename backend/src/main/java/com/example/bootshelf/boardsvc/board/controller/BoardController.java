package com.example.bootshelf.boardsvc.board.controller;

import com.example.bootshelf.boardsvc.board.model.request.PatchUpdateBoardReq;
import com.example.bootshelf.boardsvc.board.model.request.PostCreateBoardReq;
import com.example.bootshelf.boardsvc.board.model.response.PostCreateBoardRes;
import com.example.bootshelf.boardsvc.board.service.BoardService;
import com.example.bootshelf.common.BaseRes;
import com.example.bootshelf.user.model.entity.User;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

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
}
