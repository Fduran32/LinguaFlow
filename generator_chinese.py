# -*- coding: utf-8 -*-
from seed_helpers import make_sublevel, make_exam

def get_chinese_a2_to_c2():
    levels = []

    # ------------------- A2 -------------------
    a2_topics = [
        ("日常生活与作息", "我每天早上七点起床。", "Wǒ měitiān zǎoshang qī diǎn qǐchuáng.", "七点", ["七点", "六点", "八点", "十点"], "我每天早上七点起床", ["我", "每天", "早上", "七点", "起床", "睡觉"], "我每天早上七点起床", "我每天早上七点起床。"),
        ("购物与价格", "请问这个苹果多少钱一斤？", "Qǐngwèn zhège píngguǒ duōshǎo qián yì jīn?", "多少钱", ["多少钱", "什么时候", "什么地方", "谁的"], "太贵了能不能便宜一点儿", ["太", "贵", "了", "能不能", "便宜", "一点儿", "买"], "太贵了能不能便宜一点儿", "太贵了，能不能便宜一点儿？"),
        ("方向与位置", "医院在银行的东边。", "Yīyuàn zài yínháng de dōngbian.", "在", ["在", "是", "去", "有"], "沿着这条路往前走", ["沿着", "这条", "路", "往", "前", "走", "看"], "沿着这条路往前走", "沿着这条路一直往前走。"),
        ("天气与季节", "今天天气很冷正在下雨。", "Jīntiān tiānqì hěn lěng zhèngzài xiàyǔ.", "下雨", ["下雨", "下雪", "晴天", "刮风"], "明天天气比今天更冷", ["明天", "天气", "比", "今天", "更", "冷", "热"], "明天天气比今天更冷", "明天天气比今天更冷。"),
        ("爱好与运动", "我周末常常跟朋友一起踢足球。", "Wǒ zhōumò chángcháng gēn péngyou yìqǐ tī zúqiú.", "踢足球", ["踢足球", "看电影", "听音乐", "打网球"], "我非常喜欢游泳和跑步", ["我", "非常", "喜欢", "游泳", "和", "跑步", "打球"], "我非常喜欢游泳和跑步", "我非常喜欢游泳和跑步。"),
        ("交通方式", "我每天坐地铁去公司上班。", "Wǒ měitiān zuò dìtiě qù gōngsī shàngbān.", "坐", ["坐", "开", "骑", "走"], "从这里到机场坐出租车很方便", ["从", "这里", "到", "机场", "坐", "出租车", "很", "方便"], "从这里到机场坐出租车很方便", "从这里到机场坐出租车很方便。"),
        ("在饭馆点菜", "服务员请给我们看一下菜单。", "Fúwùyuán qǐng gěi wǒmen kàn yíxià càidān.", "菜单", ["菜单", "买单", "筷子", "米饭"], "请来一碗牛肉面和一杯绿茶", ["请", "来", "一碗", "牛肉面", "和", "一杯", "绿茶"], "服务员请结账买单", "服务员，请给我们拿一下菜单。"),
        ("看病与健康", "我感冒了头疼得厉害。", "Wǒ gǎnmào le tóuténg de lìhai.", "感冒", ["感冒", "高兴", "累", "饿"], "医生说你需要多喝水好好休息", ["医生", "说", "你", "需要", "多", "喝水", "好好", "休息"], "医生说你需要多喝水好好休息", "医生说你需要多喝水，多休息。"),
        ("过去经历(过)", "我去过中国两次风景非常美丽。", "Wǒ qùguo Zhōngguó liǎng cì fēngjǐng fēicháng měilì.", "去过", ["去过", "去了", "在去", "想去"], "你吃过传统的北京烤鸭吗", ["你", "吃过", "传统的", "北京", "烤鸭", "吗", "想吃"], "你吃过传统的北京烤鸭吗", "我去过中国两次，风景非常美丽。"),
        ("比较与程度(比)", "哥哥比弟弟高很多。", "Gēge bǐ dìdi gāo hěn duō.", "比", ["比", "和", "跟", "像"], "中国茶比咖啡更好喝", ["中国", "茶", "比", "咖啡", "更", "好喝", "贵"], "中国茶比咖啡更好喝", "哥哥比弟弟高很多。")
    ]

    prev_id = "zh_lvl_a1_exam"
    for idx, (title, mc_audio, mc_phon, mc_ans, mc_opts, tb_sent, tb_opts, spk_sent, lis_sent) in enumerate(a2_topics, 1):
        lvl_id = f"zh_lvl_a2_{idx}"
        q_data = [
            {"type": "MULTIPLE_CHOICE", "prompt": f"Completa en mandarín ({title}):", "phonetic": mc_phon, "audio": mc_audio, "options": mc_opts, "correct": mc_ans, "explanation": f"Estructura A2 en {title}."},
            {"type": "TRANSLATE_BUILD", "prompt": f"Ordena las palabras chinas: '{tb_sent}'", "phonetic": mc_phon, "audio": tb_sent, "options": tb_opts, "correct": tb_sent, "explanation": "Orden sintáctico en chino A2."},
            {"type": "SPEAKING", "prompt": "Pronuncia en voz alta en chino mandarín:", "phonetic": mc_phon, "audio": spk_sent, "options": [], "correct": spk_sent, "explanation": "Pronunciación y tonos A2."},
            {"type": "LISTENING", "prompt": "Escucha y selecciona la opción idéntica:", "phonetic": mc_phon, "audio": lis_sent, "options": [lis_sent, lis_sent.replace("我", "你"), "我听不懂你说的话。", "请你说得慢一点儿。"], "correct": lis_sent, "explanation": f"Audio: '{lis_sent}'."}
        ]
        levels.append(make_sublevel(lvl_id, idx, title, "A2", prev_id, q_data))
        prev_id = lvl_id

    # A2 Exam
    a2_exam_q = [
        {"type": "MULTIPLE_CHOICE", "prompt": "Clasificador: '一___书' (un libro):", "phonetic": "Yì běn shū", "audio": "一本", "options": ["本 (běn)", "个 (gè)", "只 (zhī)", "张 (zhāng)"], "correct": "本 (běn)", "explanation": "本 es el clasificador para libros y cuadernos."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '我去过中国两次'", "phonetic": "Wǒ qùguo Zhōngguó liǎng cì", "audio": "我去过中国两次", "options": ["我", "去过", "中国", "两次", "去了"], "correct": "我去过中国两次", "explanation": "Uso de 过 para experiencia previa."},
        {"type": "SPEAKING", "prompt": "Pronuncia:", "phonetic": "Wǒ měitiān zǎoshang qī diǎn qǐchuáng", "audio": "我每天早上七点起床", "options": [], "correct": "我每天早上七点起床", "explanation": "Rutina diaria y horas."},
        {"type": "LISTENING", "prompt": "Escucha atentamente:", "phonetic": "Zhège píngguǒ duōshǎo qián yì jīn?", "audio": "这个苹果多少钱一斤？", "options": ["这个苹果多少钱一斤？", "那个西瓜多少钱一个？", "请问去机场怎么走？", "我们要两碗牛肉面。"], "correct": "这个苹果多少钱一斤？", "explanation": "Preguntar por precio."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Comparación: '今天___昨天热。'", "phonetic": "Jīntiān bǐ zuótiān rè", "audio": "比", "options": ["比", "和", "跟", "同"], "correct": "比", "explanation": "Patrón de comparación A 比 B + adjetivo."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '太贵了能不能便宜一点儿'", "phonetic": "Tài guì le néng bu néng piányi yìdiǎnr", "audio": "太贵了能不能便宜一点儿", "options": ["太", "贵", "了", "能不能", "便宜", "一点儿"], "correct": "太贵了能不能便宜一点儿", "explanation": "Regateo y cortesía en compras."},
        {"type": "SPEAKING", "prompt": "Pronuncia la preferencia:", "phonetic": "Zhōngguó chá bǐ kāfēi gèng hǎohē", "audio": "中国茶比咖啡更好喝", "options": [], "correct": "中国茶比咖啡更好喝", "explanation": "Comparación con 更."},
        {"type": "LISTENING", "prompt": "Escucha y responde:", "phonetic": "Cóng zhèlǐ dào jīchǎng zuò dìtiě hěn fāngbiàn", "audio": "从这里到机场坐地铁很方便。", "options": ["从这里到机场坐地铁很方便。", "从火车站坐公交车需要一小时。", "我们走路去公园非常近。", "明天下午坐飞机去上海。"], "correct": "从这里到机场坐地铁很方便。", "explanation": "Medio de transporte metro."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Aspecto progresivo: '外面正在___。'", "phonetic": "Wàimiàn zhèngzài xiàyǔ", "audio": "下雨", "options": ["下雨", "雨下", "天雨", "水下"], "correct": "下雨", "explanation": "正在下雨 (está lloviendo)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '沿着这条路一直往前走'", "phonetic": "Yánzhe zhè tiáo lù yìzhí wǎng qián zǒu", "audio": "沿着这条路一直往前走", "options": ["沿着", "这条", "路", "一直", "往", "前", "走"], "correct": "沿着这条路一直往前走", "explanation": "Direcciones: seguir todo recto."}
    ]
    levels.append(make_exam("zh_lvl_a2_exam", 11, "Examen A2", "A2", "zh_lvl_a2_10", a2_exam_q))

    # ------------------- B1 -------------------
    b1_topics = [
        ("旅行与准备", "我们提前在网上订好了往返机票。", "Wǒmen tíqián zài wǎngshàng dìnghǎo le wǎngfǎn jīpiào.", "订好", ["订好", "订过", "订着", "订在"], "出门前请仔细检查行李和护照", ["出门", "前", "请", "仔细", "检查", "行李", "和", "护照"], "我们在网上订好了往返机票", "我们提前在网上订好了往返机票。"),
        ("因果与转折", "因为今天下大雨所以足球赛取消了。", "Yīnwèi jīntiān xià dàyǔ suǒyǐ zúqiúsài qǔxiāo le.", "所以", ["所以", "但是", "而且", "不过"], "虽然学习汉语很难但是非常有趣", ["虽然", "学习", "汉语", "很", "难", "但是", "非常", "有趣"], "虽然很累但我感到非常充实", "因为今天下大雨，所以比赛延期了。"),
        ("动作的进行(着)", "老师手里拿着一本厚厚的书。", "Lǎoshī shǒulǐ názhe yì běn hòuhòu de shū.", "拿着", ["拿着", "拿了", "拿过", "拿在"], "墙上挂着一幅中国传统山水画", ["墙上", "挂着", "一幅", "中国", "传统", "山水画"], "门开着请大家直接走进来", "老师手里拿着一本厚厚的书。"),
        ("结果与趋向补语", "我终于听懂了老师讲的语法点。", "Wǒ zhōngyú tīngdǒng le lǎoshī jiǎng de yǔfǎ diǎn.", "听懂", ["听懂", "听见", "听到", "听完"], "请大家把书包里的书拿出来", ["请", "大家", "把", "书包", "里的", "书", "拿出来"], "这篇文章我全部看完了", "我终于听懂了老师讲的语法重点。"),
        ("存现句与空间", "桌子上放着一杯热气腾腾的茶。", "Zhuōzi shàng fàngzhe yì bēi rèqì téngténg de chá.", "放着", ["放着", "放了", "放过", "在放"], "教室前面坐着很多来自各国的留学生", ["教室", "前面", "坐着", "很多", "来自", "各国的", "留学生"], "楼下停着几辆黑色的自行车", "桌子上放着一杯热气腾腾的绿茶。"),
        ("社交与借还", "这本书我借给你看下周还给我。", "Zhè běn shū wǒ jiè gěi nǐ kàn xià zhōu huán gěi wǒ.", "还", ["还", "给", "借", "买"], "真不好意思打扰你休息了", ["真", "不好意思", "打扰", "你", "休息", "了"], "谢谢你的热情款待下次我请客", "这本书借给你看，下周记得还给我。"),
        ("环保与生活", "保护环境我们要从垃圾分类开始。", "Bǎohù huánjìng wǒmen yào cóng lājī fēnlèi kāishǐ.", "分类", ["分类", "分成", "分在", "分别"], "节约用水用电是每个人的责任", ["节约", "用水", "用电", "是", "每个人的", "责任"], "出门自备环保袋减少塑料污染", "保护环境，我们要从小事做起。"),
        ("节日与传统", "春节是全家团圆吃饺子的日子。", "Chūnjié shì quánjiā tuányuán chī jiǎozi de rìzi.", "团圆", ["团圆", "聚会", "开会", "集合"], "中秋节一家人一边赏月一边吃月饼", ["中秋节", "一家人", "一边", "赏月", "一边", "吃月饼"], "祝大家新春快乐阖家幸福安康", "春节是全家团聚吃年夜饭的重要节日。"),
        ("可能补语", "这么多作业我今天晚上做不完。", "Zhème duō zuòyè wǒ jīntiān wǎnshang zuò bu wán.", "做不完", ["做不完", "做没完", "做不完的", "没做完"], "字写得太小了我看不清楚", ["字", "写得", "太", "小", "了", "我", "看不清楚"], "声音太小了后面的人听不到", "作业太多了，我今天晚上做不完。"),
        ("愿望与计划", "我打算明年申请去北京大学留学。", "Wǒ dǎsuàn míngnián shēnqǐng qù Běijīng Dàxué liúxué.", "打算", ["打算", "希望", "想要", "以为"], "我希望能够成为一名优秀的翻译家", ["我", "希望", "能够", "成为", "一名", "优秀的", "翻译家"], "毕业以后我想在外企从事跨国贸易", "我打算明年申请去中国留学深造。")
    ]

    prev_id = "zh_lvl_a2_exam"
    for idx, (title, mc_audio, mc_phon, mc_ans, mc_opts, tb_sent, tb_opts, spk_sent, lis_sent) in enumerate(b1_topics, 1):
        lvl_id = f"zh_lvl_b1_{idx}"
        q_data = [
            {"type": "MULTIPLE_CHOICE", "prompt": f"Gramática B1 ({title}):", "phonetic": mc_phon, "audio": mc_audio, "options": mc_opts, "correct": mc_ans, "explanation": f"Estructura intermedia B1 en {title}."},
            {"type": "TRANSLATE_BUILD", "prompt": f"Ordena las fichas chinas: '{tb_sent}'", "phonetic": mc_phon, "audio": tb_sent, "options": tb_opts, "correct": tb_sent, "explanation": "Sintaxis B1 en mandarín."},
            {"type": "SPEAKING", "prompt": "Pronuncia con fluidez y entonación precisa:", "phonetic": mc_phon, "audio": spk_sent, "options": [], "correct": spk_sent, "explanation": "Competencia oral B1."},
            {"type": "LISTENING", "prompt": "Escucha atentamente el enunciado:", "phonetic": mc_phon, "audio": lis_sent, "options": [lis_sent, lis_sent.replace("我", "他"), "我们要马上出发去机场。", "明天早上八点在校门口集合。"], "correct": lis_sent, "explanation": f"Audio: '{lis_sent}'."}
        ]
        levels.append(make_sublevel(lvl_id, idx, title, "B1", prev_id, q_data))
        prev_id = lvl_id

    # B1 Exam
    b1_exam_q = [
        {"type": "MULTIPLE_CHOICE", "prompt": "Correlativo: '虽然天气很冷，___他坚持跑步。'", "phonetic": "Suīrán tiānqì hěn lěng, dànshì...", "audio": "但是", "options": ["但是", "所以", "因为", "或者"], "correct": "但是", "explanation": "Par Aunque... pero (虽然...但是)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '桌子上放着一杯热气腾腾的茶'", "phonetic": "Zhuōzi shàng fàngzhe yì bēi chá", "audio": "桌子上放着一杯热气腾腾的茶", "options": ["桌子上", "放着", "一杯", "热气腾腾的", "茶"], "correct": "桌子上放着一杯热气腾腾的茶", "explanation": "Oración existencial (存现句)."},
        {"type": "SPEAKING", "prompt": "Pronuncia:", "phonetic": "Wǒ zhōngyú tīngdǒng le lǎoshī jiǎng de kè", "audio": "我终于听懂了老师讲的课", "options": [], "correct": "我终于听懂了老师讲的课", "explanation": "Complemento de resultado 听懂."},
        {"type": "LISTENING", "prompt": "Escucha:", "phonetic": "Wǒmen tíqián zài wǎngshàng dìnghǎo le jīpiào", "audio": "我们提前在网上订好了机票。", "options": ["我们提前在网上订好了机票。", "我们昨天去火车站买了车票。", "机票价格比平时便宜很多。", "下个月我们要去南方旅游。"], "correct": "我们提前在网上订好了机票。", "explanation": "订好了机票."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Complemento potencial: '字太小了，我___。'", "phonetic": "Zì tài xiǎo le, wǒ kàn bu qīngchu", "audio": "看不清楚", "options": ["看不清楚", "看没清楚", "看不清的", "看不是清"], "correct": "看不清楚", "explanation": "看不清楚 (no poder ver con claridad)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '春节是全家团圆吃饺子的重要节日'", "phonetic": "Chūnjié shì quánjiā tuányuán de jiérì", "audio": "春节是全家团圆吃饺子的重要节日", "options": ["春节", "是", "全家", "团圆", "吃饺子的", "重要", "节日"], "correct": "春节是全家团圆吃饺子的重要节日", "explanation": "Expresiones culturales chinas."},
        {"type": "SPEAKING", "prompt": "Pronuncia la meta académica:", "phonetic": "Wǒ dǎsuàn míngnián shēnqǐng qù Běijīng liúxué", "audio": "我打算明年申请去北京留学", "options": [], "correct": "我打算明年申请去北京留学", "explanation": "Expresar planes futuros."},
        {"type": "LISTENING", "prompt": "Escucha y responde:", "phonetic": "Bǎohù huánjìng yào cóng lājī fēnlèi kāishǐ", "audio": "保护环境要从垃圾分类开始。", "options": ["保护环境要从垃圾分类开始。", "节约用水是大家共同的责任。", "我们每天都应该多去锻炼身体。", "城市绿化工作取得了很大成就。"], "correct": "保护环境要从垃圾分类开始。", "explanation": "Medio ambiente y reciclaje."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Aspecto durativo: '门___呢，快进来吧。'", "phonetic": "Mén kāizhe ne", "audio": "开着", "options": ["开着", "开了", "开过", "在开"], "correct": "开着", "explanation": "着 indica estado continuo (la puerta está abierta)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '请大家把书包里的课本拿出来'", "phonetic": "Qǐng dàjiā bǎ shūbāo lǐ de kèběn ná chūlái", "audio": "请大家把书包里的课本拿出来", "options": ["请", "大家", "把", "书包里的", "课本", "拿出来"], "correct": "请大家把书包里的课本拿出来", "explanation": "Estructura 把 con complemento direccional."}
    ]
    levels.append(make_exam("zh_lvl_b1_exam", 11, "Examen B1", "B1", "zh_lvl_b1_10", b1_exam_q))

    # ------------------- B2 -------------------
    b2_topics = [
        ("把字句深度掌握", "请你先把房间里的垃圾收拾干净。", "Qǐng nǐ xiān bǎ fángjiān lǐ de lājī shōushi gānjìng.", "收拾干净", ["收拾干净", "收拾", "在收拾", "干净"], "我们必须把这项紧急任务按时完成", ["我们", "必须", "把", "这项", "紧急", "任务", "按时", "完成"], "请把这份合同打印出来给经理签字", "请你先把房间里的垃圾收拾干净。"),
        ("被字句与被动语态", "桌子上的蛋糕被小猫偷吃掉了。", "Zhuōzi shàng de dàngāo bèi xiǎomāo tōuchī diào le.", "被", ["被", "把", "叫", "给"], "他的科研论文被国际权威期刊录用了", ["他的", "科研", "论文", "被", "国际", "权威", "期刊", "录用了"], "自行车被偷了这让我感到非常郁闷", "桌子上的蛋糕被小猫偷吃掉了。"),
        ("越来越与越...越...", "随着科技的发展生活变得越来越便捷。", "Suízhe kējì de fāzhǎn shēnghuó biànde yuè lái yuè biànjié.", "越来越", ["越来越", "更", "很", "非常"], "汉语水平越高对中国文化理解越深刻", ["汉语", "水平", "越高", "对", "中国", "文化", "理解", "越深刻"], "天气越来越暖和树木都发芽了", "随着科技的发展，生活变得越来越便捷。"),
        ("递进复句(不仅...而且)", "他不仅球踢得好而且学习成绩优异。", "Tā bùjǐn qiú tī de hǎo érqiě xuéxí chéngjì yōuyì.", "不仅", ["不仅", "虽然", "如果", "即使"], "这部电影不仅情节感人而且画面精美绝伦", ["这部", "电影", "不仅", "情节", "感人", "而且", "画面", "精美绝伦"], "多掌握一门外语不仅开阔眼界更能提升竞争力", "他不仅足球踢得好，而且品学兼优。"),
        ("无条件句(无论...都)", "无论遇到多大困难我们都要迎难而上。", "Wúlùn yùdào duō dà kùnnan wǒmen dōu yào yíngnán'érshàng.", "无论", ["无论", "尽管", "只要", "只有"], "无论明天下不下雨活动都照常举行", ["无论", "明天", "下不下雨", "活动", "都", "照常", "举行"], "无论身处何地都不要忘记初心和使命", "无论遇到多大阻碍，我们都要坚定前行。"),
        ("工作求职与面试", "面试时展现自信和专业素养至关重要。", "Miànshì shí zhǎnxiàn zìxìn hé zhuānyè sùyǎng zhìguān zhòngyào.", "至关重要", ["至关重要", "随便", "无所谓", "一般"], "请在个人简历中突出你的项目实践经验", ["请", "在", "个人", "简历", "中", "突出", "你的", "项目", "实践经验"], "我对贵公司的发展前景抱有极高期望", "面试时展现自信与沉稳至关重要。"),
        ("现代科技与网络", "人工智能正在深刻改变人类的生产生活方式。", "Réngōng zhìnéng zhèngzài shēnkè gǎibiàn rénlèi de shēngchǎn shēnghuó fāngshì.", "深刻改变", ["深刻改变", "不变", "停留", "减少"], "大数据和云计算加速了产业数字化转型", ["大数据", "和", "云计算", "加速了", "产业", "数字化", "转型"], "智能手机普及极大便利了大众日常出行", "人工智能正在重塑各行各业的未来格局。"),
        ("成语入门与运用", "到了国外我们应该入乡随俗尊重当地文化。", "Dào le guówài wǒmen yīnggāi rùxiāngsuísú zūnzhòng dāngdì wénhuà.", "入乡随俗", ["入乡随俗", "一心一意", "马马虎虎", "半途而废"], "只要坚持不懈再难的关卡也能攻克", ["只要", "坚持不懈", "再难的", "关卡", "也", "能", "攻克"], "做任何事情都不能三心二意半途而废", "入乡随俗是跨文化交际的重要原则。"),
        ("观点辩论与陈述", "在我看来全面客观地看待问题尤为关键。", "Zài wǒ kànlái quánmiàn kèguān de kàndài wèntí yóuwéi guānjiàn.", "在我看来", ["在我看来", "随便说说", "我不清楚", "无可奉告"], "我赞同你的观点但有些细节值得商榷", ["我", "赞同", "你的", "观点", "但", "有些", "细节", "值得", "商榷"], "我们需要在经济发展与生态保护之间取得平衡", "在我看来，多角度审视问题才能得出正确结论。"),
        ("复杂情态与评价", "面对不可抗力产生损失往往在所难免。", "Miànduì bùkěkànglì chǎnshēng sǔnshī wǎngwǎng zàisuǒnánmiǎn.", "在所难免", ["在所难免", "可以避免", "轻而易举", "顺理成章"], "这部严谨的学术专著非常值得深入研读", ["这部", "严谨的", "学术", "专著", "非常", "值得", "深入", "研读"], "我们应当遵守国际准则维护公平正义", "在激烈竞争中出现波动在所难免。")
    ]

    prev_id = "zh_lvl_b1_exam"
    for idx, (title, mc_audio, mc_phon, mc_ans, mc_opts, tb_sent, tb_opts, spk_sent, lis_sent) in enumerate(b2_topics, 1):
        lvl_id = f"zh_lvl_b2_{idx}"
        q_data = [
            {"type": "MULTIPLE_CHOICE", "prompt": f"Estructura avanzada B2 ({title}):", "phonetic": mc_phon, "audio": mc_audio, "options": mc_opts, "correct": mc_ans, "explanation": f"Uso B2 avanzado en {title}."},
            {"type": "TRANSLATE_BUILD", "prompt": f"Ordena las fichas: '{tb_sent}'", "phonetic": mc_phon, "audio": tb_sent, "options": tb_opts, "correct": tb_sent, "explanation": "Construcción compleja B2 en chino."},
            {"type": "SPEAKING", "prompt": "Pronuncia con precisión tonal y ritmo nativo:", "phonetic": mc_phon, "audio": spk_sent, "options": [], "correct": spk_sent, "explanation": "Fluidez y pronunciación B2."},
            {"type": "LISTENING", "prompt": "Escucha y selecciona la declaración precisa:", "phonetic": mc_phon, "audio": lis_sent, "options": [lis_sent, lis_sent.replace("我们", "大家"), "会议决定推迟项目发布时间。", "双方就协议条款达成了高度一致。"], "correct": lis_sent, "explanation": f"Audio: '{lis_sent}'."}
        ]
        levels.append(make_sublevel(lvl_id, idx, title, "B2", prev_id, q_data))
        prev_id = lvl_id

    # B2 Exam
    b2_exam_q = [
        {"type": "MULTIPLE_CHOICE", "prompt": "Estructura 把: '请把这份重要文件___。'", "phonetic": "Qǐng bǎ zhè fèn zhòngyào wénjiàn jiāogěi jīnglǐ", "audio": "交给我", "options": ["交给我", "看", "写", "在桌子"], "correct": "交给我", "explanation": "把 requiere complemento de resultado (entregar a)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '他的学术论文被权威期刊录用了'", "phonetic": "Tā de xuéshù lùnwén bèi quánwēi qīkān lùyòng le", "audio": "他的学术论文被权威期刊录用了", "options": ["他的", "学术论文", "被", "权威期刊", "录用了"], "correct": "他的学术论文被权威期刊录用了", "explanation": "Estructura pasiva con 被."},
        {"type": "SPEAKING", "prompt": "Pronuncia el principio incondicional:", "phonetic": "Wúlùn miànduì duō dà kùnnan wǒmen dōu bù tuìsuō", "audio": "无论面对多大困难我们都不退缩", "options": [], "correct": "无论面对多大困难我们都不退缩", "explanation": "无论...都."},
        {"type": "LISTENING", "prompt": "Escucha atentamente:", "phonetic": "Suízhe kējì jīnbù shēnghuó biànde yuè lái yuè biànjié", "audio": "随着科技进步生活变得越来越便捷。", "options": ["随着科技进步生活变得越来越便捷。", "近年来城市经济保持平稳快速增长。", "我们要加强数字基础设施建设投资。", "绿色低碳理念已经深入千家万户。"], "correct": "随着科技进步生活变得越来越便捷。", "explanation": "越来越便捷."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Chengyu: '去异国他乡生活应当___尊重风俗。'", "phonetic": "Rù xiāng suí sú", "audio": "入乡随俗", "options": ["入乡随俗", "半途而废", "拔苗助长", "坐井观天"], "correct": "入乡随俗", "explanation": "入乡随俗: adaptarse a las costumbres locales."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '不仅开阔了眼界而且提升了竞争力'", "phonetic": "Bùjǐn kāikuò le yǎnjiè érqiě tíshēng le jìngzhēnglì", "audio": "不仅开阔了眼界而且提升了竞争力", "options": ["不仅", "开阔了", "眼界", "而且", "提升了", "竞争力"], "correct": "不仅开阔了眼界而且提升了竞争力", "explanation": "不仅...而且."},
        {"type": "SPEAKING", "prompt": "Pronuncia la opinión estructurada:", "phonetic": "Zài wǒ kànlái duō jiǎodù sīkǎo wèntí zhìguān zhòngyào", "audio": "在我看来多角度思考问题至关重要", "options": [], "correct": "在我看来多角度思考问题至关重要", "explanation": "Expresar opinión formal."},
        {"type": "LISTENING", "prompt": "Escucha:", "phonetic": "Miànshì shí zhǎnxiàn chū yīngyǒu de zhuānyè sùyǎng", "audio": "面试时展现出应有的专业素养。", "options": ["面试时展现出应有的专业素养。", "求职者要认真撰写自己的个人简历。", "人事部门下周统一安排复试考核。", "公司为新员工提供完善的入职培训。"], "correct": "面试时展现出应有的专业素养。", "explanation": "专业素养."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Progresión correlativa: '汉语学得越深，___觉得其内涵丰富。'", "phonetic": "Yuè... yuè...", "audio": "越", "options": ["越", "更", "很", "最"], "correct": "越", "explanation": "Patrón: 越...越... (mientras más... más...)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '我们必须把这项紧急任务按时完成'", "phonetic": "Wǒmen bìxū bǎ zhè xiàng jǐnjí rènwu ànshí wánchéng", "audio": "我们必须把这项紧急任务按时完成", "options": ["我们", "必须", "把", "这项", "紧急任务", "按时", "完成"], "correct": "我们必须把这项紧急任务按时完成", "explanation": "Estructura 把 en contexto laboral."}
    ]
    levels.append(make_exam("zh_lvl_b2_exam", 11, "Examen B2", "B2", "zh_lvl_b2_10", b2_exam_q))

    # ------------------- C1 -------------------
    c1_topics = [
        ("成语与典故", "得知危机全面化解大家顿时如释重负。", "Dézhī wēijī quánmiàn huàjiě dàjiā dùnshí rúshìzhòngfù.", "如释重负", ["如释重负", "望梅止渴", "掩耳盗铃", "叶公好龙"], "我们要拿出破釜沉舟的勇气开创新局面", ["我们要", "拿出", "破釜沉舟的", "勇气", "开创", "新局面"], "卧薪尝胆多年终获举世瞩目的成功", "得知险情化解，大家心里如释重负。"),
        ("语气副词与强调", "他居然在如此短暂的时间内攻克了学术难题。", "Tā jūrán zài rúcǐ duǎnzàn de shíjiān nèi gōngkè le xuéshù nántí.", "居然", ["居然", "果然", "既然", "虽然"], "事情的发展偏偏出乎了所有专家的预料", ["事情的", "发展", "偏偏", "出乎了", "所有", "专家的", "预料"], "幸亏抢救及时否则后果简直不堪设想", "他居然在三天之内高质量完成了全部翻译。"),
        ("偏正与选择复句", "与其怨天尤人不如脚踏实地努力奋斗。", "Yǔqí yuàntiānyóurén bùrú jiǎotàshídì nǔlì fèndòu.", "不如", ["不如", "宁可", "而是", "所以"], "宁可历经千辛万苦也决不轻言放弃", ["宁可", "历经", "千辛万苦", "也", "决不", "轻言", "放弃"], "与其盲目跟风不如打造自身的特色品牌", "与其坐而论道，不如起而行之。"),
        ("书面语与公文", "鉴于当前国际局势双方决定深化战略协作。", "Jiànyú dāngqián guójì júshì shuāngfāng juédìng shēnhuà zhànlüè xiézuò.", "鉴于", ["鉴于", "关于", "由于", "至于"], "有关部门对违规行为予以严肃查处通报", ["有关", "部门", "对", "违规行为", "予以", "严肃", "查处", "通报"], "本项学术基金旨在资助青年学者潜心研究", "鉴于客观条件变化，会议决议作相应调整。"),
        ("商业谈判与金融", "双方就跨国并购核心条款达成原则性共识。", "Shuāngfāng jiù kuàguó bìnggòu héxīn tiáokuǎn dáchéng yuánzéxìng gòngshí.", "原则性共识", ["原则性共识", "随便协议", "口头约定", "小幅分歧"], "多元化资产配置能够有效平抑投资组合波动", ["多元化", "资产配置", "能够", "有效", "平抑", "投资组合", "波动"], "防范系统性金融风险是宏观调控的底线要求", "双方就股权架构及估值方案展开了务实磋商。"),
        ("社会热点深度分析", "人口老龄化加剧对社会保障体系提出严峻考验。", "Rénkǒu lǎolínghuà jiājù duì shèhuì bǎozhàng tǐxì tíchū yánjùn kǎoyàn.", "严峻考验", ["严峻考验", "小事一桩", "毫无影响", "轻松过关"], "推进新型城镇化建设必须坚持以人为本理念", ["推进", "新型", "城镇化", "建设", "必须", "坚持", "以人为本", "理念"], "缩小收入差距促进社会公平是现代治理基石", "积极应对老龄化成为国家中长期重大战略。"),
        ("传统哲学与文化", "道法自然天人合一蕴含着深刻的东方智慧。", "Dàofǎzìrán tiānrén héyī yùnhánzhe shēnkè de dōngfāng zhìhuì.", "蕴含", ["蕴含", "含有", "带着", "藏在"], "儒家讲求仁义礼智信修身齐家治国平天下", ["儒家", "讲求", "仁义礼智信", "修身", "齐家", "治国", "平天下"], "辩证思维强调矛盾双方在一定条件下相互转化", "天行健君子以自强不息是中华民族的精神风貌。"),
        ("文学意境与修辞", "月光如流水般静静地泻在荷叶和花瓣上。", "Yuèguāng rú liúshuǐ bān jìngjìng de xiè zài héyè hé huābàn shàng.", "泻", ["泻", "流", "照", "洒"], "文章辞藻华丽情理交融令人回味无穷", ["文章", "辞藻", "华丽", "情理", "交融", "令人", "回味无穷"], "作者运用精妙绝伦的比喻刻画人物内心挣扎", "清风徐来水波不兴展现出淡泊超脱的意境。"),
        ("深度新闻与社论", "媒体舆论应当秉持客观公允激浊扬清的立场。", "Méitǐ yúlùn yīnggāi bǐngchí kèguān gōngyǔn jīzhuóyángqīng de lìchǎng.", "秉持", ["秉持", "拿着", "放开", "丢掉"], "深度调查报道抽丝剥茧揭露行业潜规则真相", ["深度", "调查", "报道", "抽丝剥茧", "揭露", "行业", "潜规则", "真相"], "激浊扬清弘扬正气是新闻从业者的崇高使命", "权威社论深入剖析了宏观经济指标的深层逻辑。"),
        ("因果因由(难怪与原来)", "难怪他今天面带喜色原来项目获批了。", "Nánguài tā jīntiān miàndài xǐsè yuánlái xiàngmù huòpī le.", "原来", ["原来", "本来", "如果", "因此"], "难怪实验室彻夜通明原来科研团队有了重大突破", ["难怪", "实验室", "彻夜通明", "原来", "科研", "团队", "有了", "重大突破"], "真相大白之后所有无端猜忌均不攻自破", "难怪大家如此振奋，原来取得了历史性突破。")
    ]

    prev_id = "zh_lvl_b2_exam"
    for idx, (title, mc_audio, mc_phon, mc_ans, mc_opts, tb_sent, tb_opts, spk_sent, lis_sent) in enumerate(c1_topics, 1):
        lvl_id = f"zh_lvl_c1_{idx}"
        q_data = [
            {"type": "MULTIPLE_CHOICE", "prompt": f"Chino formal y literario C1 ({title}):", "phonetic": mc_phon, "audio": mc_audio, "options": mc_opts, "correct": mc_ans, "explanation": f"Estructura avanzada C1 en {title}."},
            {"type": "TRANSLATE_BUILD", "prompt": f"Ordena las fichas chinas: '{tb_sent}'", "phonetic": mc_phon, "audio": tb_sent, "options": tb_opts, "correct": tb_sent, "explanation": "Construcción formal C1 en mandarín."},
            {"type": "SPEAKING", "prompt": "Pronuncia con cadencia formal y elegancia académica:", "phonetic": mc_phon, "audio": spk_sent, "options": [], "correct": spk_sent, "explanation": "Elocución de alto nivel C1."},
            {"type": "LISTENING", "prompt": "Escucha atentamente el enunciado formal C1:", "phonetic": mc_phon, "audio": lis_sent, "options": [lis_sent, lis_sent.replace("我们", "全员"), "双方未能在核心利益上达成妥协。", "代表团发表声明阐述了基本立场。"], "correct": lis_sent, "explanation": f"Audio: '{lis_sent}'."}
        ]
        levels.append(make_sublevel(lvl_id, idx, title, "C1", prev_id, q_data))
        prev_id = lvl_id

    # C1 Exam
    c1_exam_q = [
        {"type": "MULTIPLE_CHOICE", "prompt": "Chengyu: '任务圆满达成，负责人长舒一口气，感到___。'", "phonetic": "Rú shì zhòng fù", "audio": "如释重负", "options": ["如释重负", "破釜沉舟", "刻舟求剑", "杯弓蛇影"], "correct": "如释重负", "explanation": "如释重负: como si se quitara una pesada carga."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '与其怨天尤人不如脚踏实地努力奋斗'", "phonetic": "Yǔqí yuàntiānyóurén bùrú jiǎotàshídì nǔlì fèndòu", "audio": "与其怨天尤人不如脚踏实地努力奋斗", "options": ["与其", "怨天尤人", "不如", "脚踏实地", "努力奋斗"], "correct": "与其怨天尤人不如脚踏实地努力奋斗", "explanation": "与其...不如."},
        {"type": "SPEAKING", "prompt": "Pronuncia la declaración institucional:", "phonetic": "Jiànyú dāngqián júshì shuāngfāng juédìng shēnhuà hézuò", "audio": "鉴于当前局势双方决定深化合作", "options": [], "correct": "鉴于当前局势双方决定深化合作", "explanation": "Registro formal escrito 鉴于."},
        {"type": "LISTENING", "prompt": "Escucha atentamente:", "phonetic": "Tā jūrán zài duǎnduǎn sān tiān nèi wánchéng le rènwu", "audio": "他居然在短短三天内完成了任务。", "options": ["他居然在短短三天内完成了任务。", "大家都在按部就班推进研发进度。", "实验结果与预期模型产生了较大偏差。", "专家组一致通过了项目验收报告。"], "correct": "他居然在短短三天内完成了任务。", "explanation": "居然 (sorpresa enfática)."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Escrito formal: '对违规违法行为依法___严惩。'", "phonetic": "Yǔyǐ", "audio": "予以", "options": ["予以", "给以", "叫给", "拿去"], "correct": "予以", "explanation": "予以 es el marcador formal de otorgar/aplicar en documentos oficiales."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '道法自然天人合一蕴含着深刻东方智慧'", "phonetic": "Dàofǎzìrán tiānrén héyī yùnhánzhe shēnkè zhìhuì", "audio": "道法自然天人合一蕴含着深刻东方智慧", "options": ["道法自然", "天人合一", "蕴含着", "深刻", "东方智慧"], "correct": "道法自然天人合一蕴含着深刻东方智慧", "explanation": "Conceptos de la filosofía tradicional china."},
        {"type": "SPEAKING", "prompt": "Pronuncia con tono solemne:", "phonetic": "Rénkǒu lǎolínghuà duì shèhuì bǎozhàng tǐxì tíchū yánjùn kǎoyàn", "audio": "人口老龄化对社会保障体系提出严峻考验", "options": [], "correct": "人口老龄化对社会保障体系提出严峻考验", "explanation": "Análisis social C1."},
        {"type": "LISTENING", "prompt": "Escucha el extracto económico:", "phonetic": "Duōyuánhuà zīchǎn pèizhì yǒuxiào píngyì le bōdòng", "audio": "多元化资产配置有效平抑了市场波动。", "options": ["多元化资产配置有效平抑了市场波动。", "央行宣布维持现行基准利率不变。", "外汇市场汇率双向宽幅震荡加剧。", "宏观逆周期调节政策发挥显著成效。"], "correct": "多元化资产配置有效平抑了市场波动。", "explanation": "平抑市场波动."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Correlativo de explicación: '难怪他如此胸有成竹，___早有万全之策。'", "phonetic": "Nánguài... yuánlái...", "audio": "原来", "options": ["原来", "本来", "如果", "因此"], "correct": "原来", "explanation": "难怪...原来 (con razón... resulta que)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '秉持客观公允立场深入剖析深层逻辑'", "phonetic": "Bǐngchí kèguān gōngyǔn lìchǎng shēnrù pōuxī shēncéng luóji", "audio": "秉持客观公允立场深入剖析深层逻辑", "options": ["秉持", "客观公允立场", "深入剖析", "深层逻辑"], "correct": "秉持客观公允立场深入剖析深层逻辑", "explanation": "Registro editorial formal."}
    ]
    levels.append(make_exam("zh_lvl_c1_exam", 11, "Examen C1", "C1", "zh_lvl_c1_10", c1_exam_q))

    # ------------------- C2 -------------------
    c2_topics = [
        ("文言实词与虚词", "敏而好学不耻下问是以谓之___也。", "Mǐn ér hào xué, bù chǐ xià wèn, shì yǐ wèi zhī wén yě.", "文", ["文", "武", "德", "道"], "温故而知新可以为师矣", ["温故", "而", "知新", "可以", "为师", "矣"], "学而不思则罔思而不学则殆", "学而时习之，不亦说乎。"),
        ("高级成语微言大义", "面对潜在系统性危机我们必须做到未雨绸缪。", "Miànduì qiánzài xìtǒngxìng wēijī wǒmen bìxū zuòdào wèiyǔchóumóu.", "未雨绸缪", ["未雨绸缪", "守株待兔", "缘木求鱼", "画蛇添足"], "恪守陈规刻舟求剑终将被时代巨浪所淘汰", ["恪守陈规", "刻舟求剑", "终将", "被", "时代巨浪", "所", "淘汰"], "韬光养晦厚积薄发方能在风云变幻中立于不败", "居安思危，未雨绸缪，方能防患于未然。"),
        ("外交辞令与公文", "两国外交声明措辞谨严字里行间体现战略考量。", "Liǎng guó wàijiāo shēngmíng cuòcí jǐnyán zìlǐhángjiān tǐxiàn zhànlüè kǎoliáng.", "措辞谨严", ["措辞谨严", "漏洞百出", "口若悬河", "词不达意"], "双方重申相互尊重主权和领土完整的庄严承诺", ["双方", "重申", "相互", "尊重", "主权", "和", "领土完整", "的", "庄严承诺"], "在求同存异基础上构建互利共赢新型国际关系", "公文通篇结构缜密，义旨深远，堪为典范。"),
        ("哲学辩证与本体论", "万物负阴而抱阳冲气以为___。", "Wànwù fù yīn ér bào yáng, chōng qì yǐ wéi hé.", "和", ["和", "平", "道", "德"], "辩证法揭示了否定之否定这一螺旋上升的客观规律", ["辩证法", "揭示了", "否定之否定", "这一", "螺旋上升", "的", "客观规律"], "事物的质变必然以量变的长期积累为前提基础", "太极生两仪，两仪生四象，四象生八卦。"),
        ("诗词鉴赏与古汉语法", "海内存知己天涯若___。", "Hǎinèi cún zhījǐ, tiānyá ruò bǐlín.", "比邻", ["比邻", "朋友", "客邻", "远人"], "大江东去浪淘尽千古风流人物", ["大江东去", "浪淘尽", "千古", "风流", "人物"], "问君能有几多愁恰似一江春水向东流", "落霞与孤鹜齐飞，秋水共长天一色。"),
        ("金融战略与宏观推演", "逆周期宏观审慎监管有效防范了金融风险跨市场___。", "Nì zhōuqī hóngguān shěnshèn jiānguǎn...", "传染蔓延", ["传染蔓延", "小幅扩散", "逐渐减弱", "自行平息"], "深入研究跨境资本流动的微观动因与宏观溢出效应", ["深入研究", "跨境", "资本流动", "的", "微观动因", "与", "宏观溢出效应"], "精准拆解影子银行嵌套产品链条阻断违约传导", "金融衍生品创新必须严格置于审慎监管框架之下。"),
        ("历史典籍引经据典", "前事不忘后事之___。", "Qián shì bù wàng, hòu shì zhī shī.", "师", ["师", "友", "戒", "鉴"], "兼听则明偏信则暗乃千古治国之名箴", ["兼听则明", "偏信则暗", "乃", "千古", "治国", "之", "名箴"], "究天人之际通古今之变成一家之言", "以铜为镜可以正衣冠，以史为镜可以知兴替。"),
        ("现代法理法治思辨", "良法善治乃法治国家最根本的实质要义。", "Liángfǎ shànzhì nǎi fǎzhì guójiā zuì gēnběn de shízhì yàoyì.", "实质要义", ["实质要义", "表面形式", "可有可无", "次要补充"], "司法裁判应当在法理情三者之间探求价值平衡", ["司法裁判", "应当", "在", "法理情", "三者之间", "探求", "价值平衡"], "法不溯及既往原则严格守护公民的信赖利益", "实质正义与程序正义相互交融支撑法治大厦。"),
        ("高阶跨文化修辞", "巧妙运用隐喻和双关展现了非凡的跨文化___。", "Qiǎomiào yùnyòng yǐnyù hé shuāngguān...", "驾驭造诣", ["驾驭造诣", "普通对话", "随便应付", "简单模仿"], "跨文化对话需要超越我者中心主义的文化偏见", ["跨文化", "对话", "需要", "超越", "我者中心主义", "的", "文化偏见"], "以包容互鉴超越文明冲突以文明共存促进人类进步", "言简意赅，辞约而旨丰，臻于化境。"),
        ("母语纯熟机敏风趣", "他谈笑间运筹帷幄樯橹灰飞___。", "Tā tánxiào jiān yùnchóuwéiwò qiánglǔ huīfēi...", "烟灭", ["烟灭", "火起", "散开", "消逝"], "诙谐幽默中暗藏机锋令人击节赞赏拍案叫绝", ["诙谐幽默", "中", "暗藏", "机锋", "令人", "击节赞赏", "拍案叫绝"], "信手拈来皆成文章其语言驾驭已臻炉火纯青之境", "应对自如，妙语连珠，展露超凡名家气度。")
    ]

    prev_id = "zh_lvl_c1_exam"
    for idx, (title, mc_audio, mc_phon, mc_ans, mc_opts, tb_sent, tb_opts, spk_sent, lis_sent) in enumerate(c2_topics, 1):
        lvl_id = f"zh_lvl_c2_{idx}"
        q_data = [
            {"type": "MULTIPLE_CHOICE", "prompt": f"Maestría lingüística clásica y contemporánea C2 ({title}):", "phonetic": mc_phon, "audio": mc_audio, "options": mc_opts, "correct": mc_ans, "explanation": f"Nivel de maestría C2 en {title}."},
            {"type": "TRANSLATE_BUILD", "prompt": f"Ordena las fichas: '{tb_sent}'", "phonetic": mc_phon, "audio": tb_sent, "options": tb_opts, "correct": tb_sent, "explanation": "Sintaxis clásica/académica C2."},
            {"type": "SPEAKING", "prompt": "Pronuncia con maestría y cadencia de erudito clásico:", "phonetic": mc_phon, "audio": spk_sent, "options": [], "correct": spk_sent, "explanation": "Elocución y maestría C2."},
            {"type": "LISTENING", "prompt": "Escucha atentamente el enunciado magistral C2:", "phonetic": mc_phon, "audio": lis_sent, "options": [lis_sent, lis_sent.replace("其", "此"), "学术界就该断代问题各执一词争论不休。", "铭文记载印证了古籍流传的真实性。"], "correct": lis_sent, "explanation": f"Audio: '{lis_sent}'."}
        ]
        levels.append(make_sublevel(lvl_id, idx, title, "C2", prev_id, q_data))
        prev_id = lvl_id

    # C2 Exam
    c2_exam_q = [
        {"type": "MULTIPLE_CHOICE", "prompt": "Chengyu de alta literatura: ¿Qué crítica '刻舟求剑'?", "phonetic": "Kè zhōu qiú jiàn", "audio": "刻舟求剑", "options": ["盲目拘泥成法而不知因应客观时势变迁", "在河里寻找丢失的宝剑", "比喻意志坚决不达目的誓不罢休", "形容记忆力极佳能记住过去的事"], "correct": "盲目拘泥成法而不知因应客观时势变迁", "explanation": "刻舟求剑 satiriza la obstinación de no adaptarse a las circunstancias cambiantes."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '敏而好学不耻下问是以谓之文也'", "phonetic": "Mǐn ér hào xué bù chǐ xià wèn shì yǐ wèi zhī wén yě", "audio": "敏而好学不耻下问是以谓之文也", "options": ["敏而好学", "不耻下问", "是以", "谓之", "文也"], "correct": "敏而好学不耻下问是以谓之文也", "explanation": "Texto clásico de Las Analectas de Confucio."},
        {"type": "SPEAKING", "prompt": "Pronuncia el aforismo clásico:", "phonetic": "Hǎinèi cún zhījǐ tiānyá ruò bǐlín", "audio": "海内存知己天涯若比邻", "options": [], "correct": "海内存知己天涯若比邻", "explanation": "Poema clásico de la dinastía Tang (Wang Bo)."},
        {"type": "LISTENING", "prompt": "Escucha el pasaje de oratoria magistral:", "phonetic": "Yǐ tóng wéi jìng kěyǐ zhèng yīguān, yǐ shǐ wéi jìng kěyǐ zhī xīngtì", "audio": "以铜为镜可以正衣冠，以史为镜可以知兴替。", "options": ["以铜为镜可以正衣冠，以史为镜可以知兴替。", "学而不思则罔，思而不学则殆。", "不积跬步，无以至千里；不积小流，无以成江海。", "天下难事必作于易，天下大事必作于细。"], "correct": "以铜为镜可以正衣冠，以史为镜可以知兴替。", "explanation": "Aforismo histórico del emperador Tang Taizong."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Partícula clásica (文言文): '赤子___心' (Chìzǐ zhī xīn):", "phonetic": "Zhī", "audio": "之", "options": ["之", "的", "得", "地"], "correct": "之", "explanation": "'之' cumple función de marcador genitivo en chino clásico."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '前事不忘后事之师是千古治国名箴'", "phonetic": "Qián shì bù wàng hòu shì zhī shī", "audio": "前事不忘后事之师是千古治国名箴", "options": ["前事不忘", "后事之师", "是", "千古", "治国名箴"], "correct": "前事不忘后事之师是千古治国名箴", "explanation": "Proverbio de la crónica Zhan Guo Ce."},
        {"type": "SPEAKING", "prompt": "Pronuncia con solemnidad diplomática:", "phonetic": "Zài qiútóngcúnyì jīchǔ shàng gòujiàn hùlì gòngyíng xīn xíng guójì guānxì", "audio": "在求同存异基础上构建互利共赢新型国际关系", "options": [], "correct": "在求同存异基础上构建互利共赢新型国际关系", "explanation": "Registro diplomático contemporáneo."},
        {"type": "LISTENING", "prompt": "Escucha la sentencia jurisprudencial:", "phonetic": "Liángfǎ shànzhì nǎi fǎzhì guójiā zuì gēnběn de shízhì yàoyì", "audio": "良法善治乃法治国家最根本的实质要义。", "options": ["良法善治乃法治国家最根本的实质要义。", "司法独立是现代宪政秩序的核心基石。", "法律面前人人平等是至高无上的法治原则。", "法律的权威源于人民发自内心的拥护与信仰。"], "correct": "良法善治乃法治国家最根本的实质要义。", "explanation": "Filosofía del derecho."},
        {"type": "MULTIPLE_CHOICE", "prompt": "Modismo chengyu clásico: ¿Qué expresa '未雨绸缪'?", "phonetic": "Wèi yǔ chóu móu", "audio": "未雨绸缪", "options": ["事先做好周密防备以防患于未然", "等天下雨再去修理屋顶", "遇到问题时临渴掘井", "对于过去发生的事后悔莫及"], "correct": "事先做好周密防备以防患于未然", "explanation": "未雨绸缪: Tomar precauciones antes de la adversidad."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '信手拈来皆成文章其语言驾驭已臻炉火纯青之境'", "phonetic": "Xìn shǒu niān lái jiē chéng wénzhāng", "audio": "信手拈来皆成文章其语言驾驭已臻炉火纯青之境", "options": ["信手拈来", "皆成文章", "其语言驾驭", "已臻", "炉火纯青之境"], "correct": "信手拈来皆成文章其语言驾驭已臻炉火纯青之境", "explanation": "Máxima expresión de maestría nativa C2."}
    ]
    levels.append(make_exam("zh_lvl_c2_exam", 11, "Examen C2", "C2", "zh_lvl_c2_10", c2_exam_q))

    return levels

if __name__ == "__main__":
    lvls = get_chinese_a2_to_c2()
    print(f"Generated {len(lvls)} Chinese levels (A2..C2).")
