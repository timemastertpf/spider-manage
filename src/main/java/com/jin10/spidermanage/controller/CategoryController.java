package com.jin10.spidermanage.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jin10.spidermanage.bean.BaseResponse;
import com.jin10.spidermanage.entity.Category;
import com.jin10.spidermanage.entity.ImgUrl;
import com.jin10.spidermanage.entity.Label;
import com.jin10.spidermanage.mapper.CategoryMapper;
import com.jin10.spidermanage.service.CategoryService;
import com.jin10.spidermanage.service.ImgUrlService;
import com.jin10.spidermanage.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.util.*;

@RestController
@RequestMapping("/group")
//@CrossOrigin(origins = {"*","null"},allowCredentials="true")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private ImgUrlService imgUrlService;

    @GetMapping("/all")
    public BaseResponse getAll() {
//        return BaseResponse.ok(categoryService.getAll());
        return BaseResponse.ok(categoryService.getAllTest());

    }

    @GetMapping("/get/{gid}")
    public BaseResponse getById(@PathVariable("gid") Integer id) {
        return BaseResponse.ok(categoryService.getById(id));
    }


    @GetMapping("/add")
    public BaseResponse add(@RequestParam String category) {
        return BaseResponse.ok(categoryService.save(new Category().setCategoryName(category)));
    }

    @GetMapping("/update")
    public BaseResponse update(@RequestParam("gid") Integer id, @RequestParam("category") String category) {
        return BaseResponse.ok(categoryService.update(id, category));
    }

    @GetMapping("/delete/{gid}")
    public BaseResponse delete(@PathVariable("gid") Integer gid) {
        if (ObjectUtil.isNotNull(gid)) {
            return BaseResponse.ok(categoryService.delete(gid));
        } else {
            return BaseResponse.error(401, "参数为空");
        }
    }
}
