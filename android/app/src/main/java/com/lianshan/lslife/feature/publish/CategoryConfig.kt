package com.lianshan.lslife.feature.publish

data class CategoryConfig(
    val id: String,
    val name: String,
    /** 使用引导填写（种类/品牌/参数），不再展示属性芯片 */
    val guidedFill: Boolean = false,
    val attr1Label: String? = null,
    val attr1Options: List<String> = emptyList(),
    val attr2Label: String? = null,
    val attr2Options: List<String> = emptyList(),
)

/** 个人闲置：商品/服务种类快捷建议 */
val secondHandKindSuggestions = listOf(
    "手机数码", "电脑办公", "家用电器", "服饰鞋包",
    "家具家居", "母婴用品", "美妆护肤", "运动户外",
    "图书文娱", "宠物用品", "本地服务", "其他闲置",
)

/** 个人闲置：品牌快捷建议（点选后写入输入框，可再改） */
val secondHandBrandSuggestions = listOf(
    "Insta360", "Apple", "华为", "小米", "OPPO", "vivo", "三星", "大疆", "联想", "其他",
)

/** 个人闲置：成色（紧凑单选，写入 condition） */
val secondHandConditionOptions = listOf(
    "全新", "几乎全新", "轻微使用痕迹", "明显使用痕迹",
)

val publishCategoryConfigs = listOf(
    CategoryConfig(
        id = "second_hand",
        name = "个人闲置",
        guidedFill = true,
    ),
    CategoryConfig(
        id = "job",
        name = "招聘",
        attr1Label = "类型",
        attr1Options = listOf("全职", "兼职", "实习"),
        attr2Label = "经验",
        attr2Options = listOf("不限", "1年以内", "1-3年", "3-5年", "5年以上"),
    ),
    CategoryConfig(
        id = "house",
        name = "房租租售",
        attr1Label = "方式",
        attr1Options = listOf("整租", "合租", "出售"),
        attr2Label = "户型",
        attr2Options = listOf("1室", "2室", "3室", "4室及以上"),
    ),
    CategoryConfig(
        id = "housekeeping",
        name = "家政保洁",
        attr1Label = "服务",
        attr1Options = listOf("日常保洁", "深度保洁", "开荒保洁", "月嫂/保姆"),
    ),
    CategoryConfig(
        id = "maintenance",
        name = "水电维修",
        attr1Label = "类别",
        attr1Options = listOf("家电维修", "水管维修", "电路维修", "房屋修缮"),
    ),
    CategoryConfig(
        id = "moving",
        name = "货运搬家",
        attr1Label = "车型",
        attr1Options = listOf("小面包车", "中面包车", "小货车", "中货车"),
        attr2Label = "搬运",
        attr2Options = listOf("需搬运", "仅拉货"),
    ),
    CategoryConfig(
        id = "veggies",
        name = "水果蔬菜",
        attr1Label = "分类",
        attr1Options = listOf("新鲜水果", "蔬菜", "农副产品"),
    ),
)

fun getCategoryConfig(categoryId: String): CategoryConfig {
    return publishCategoryConfigs.find { it.id == categoryId } ?: publishCategoryConfigs.first()
}

/** 根据引导字段拼一段可编辑的描述草稿 */
fun buildGuidedDescription(
    itemKind: String,
    brand: String,
    params: String,
    condition: String,
    purchaseDate: String = "",
): String {
    val lines = mutableListOf<String>()
    val head = listOfNotNull(
        itemKind.takeIf { it.isNotBlank() },
        brand.takeIf { it.isNotBlank() },
        params.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
    if (head.isNotBlank()) lines += head
    if (condition.isNotBlank()) lines += "成色：$condition"
    if (purchaseDate.isNotBlank()) lines += "购买日期：$purchaseDate"
    if (lines.isEmpty()) return ""
    lines += "同城自提优先，有意私聊。"
    return lines.joinToString("\n")
}
