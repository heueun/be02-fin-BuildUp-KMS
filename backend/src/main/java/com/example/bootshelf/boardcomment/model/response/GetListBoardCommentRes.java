package com.example.bootshelf.boardcomment.model.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetListBoardCommentRes {
    private Integer idx;
    private Integer boardIdx;
    private Integer userIdx;
    private String nickName;
    private String boardCommnetContent;
    private String createAt;
    private String updateAt;
    private List<GetListBoardCommentRes> children; // 자식 댓글 목록

}