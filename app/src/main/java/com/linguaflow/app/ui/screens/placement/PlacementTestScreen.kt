package com.linguaflow.app.ui.screens.placement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.domain.model.AnswerResult
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.domain.model.QuestionType
import com.linguaflow.app.domain.usecase.EvaluateAnswerUseCase
import com.linguaflow.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementTestScreen(
    languageId: String,
    languageName: String,
    currentAssignedCefr: String = "A1",
    onPlacementFinished: (startingLevelOrder: Int, cefrCode: String, scorePercentage: Int) -> Unit,
    onClose: () -> Unit
) {
    // Definición de las 5 preguntas por etapa para cada nivel (A1, A2, B1, B2, C1, C2)
    val stages = listOf("A1", "A2", "B1", "B2", "C1", "C2")

    fun getQuestionsForStage(lang: String, stage: String): List<Question> {
        return when (lang) {
            "it" -> when (stage) {
                "A1" -> listOf(
                    Question("it_a1_1", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice 'Buenos días' en italiano?", null, "Buongiorno", listOf("Buongiorno", "Buonanotte", "Arrivederci", "Ciao"), "Buongiorno", "'Buongiorno' se usa durante el día."),
                    Question("it_a1_2", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se responde cortésmente a 'Grazie'?", null, "Prego", listOf("Prego", "Ciao", "Arrivederci", "Per favore"), "Prego", "'Prego' significa de nada."),
                    Question("it_a1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Io ___ uno studente.'", null, "sono", listOf("sono", "sei", "è", "siamo"), "sono", "Primera persona singular de essere: Io sono."),
                    Question("it_a1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Traduce 'Por favor':", null, "Per favore", listOf("Per favore", "Grazie", "Scusa", "Prego"), "Per favore", "'Per favore' es por favor."),
                    Question("it_a1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Piacere di ___.' (Mucho gusto)", null, "conoscerti", listOf("conoscerti", "vedere", "parlare", "stare"), "conoscerti", "'Piacere di conoscerti' es gusto en conocerte.")
                )
                "A2" -> listOf(
                    Question("it_a2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Noi ___ a Roma da due anni.'", null, "abitiamo", listOf("abitiamo", "abita", "abito", "abitano"), "abitiamo", "Desinencia de Noi: -iamo."),
                    Question("it_a2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Elige el plural correcto de 'la ragazza':", null, "le ragazze", listOf("le ragazze", "i ragazzi", "le ragazzi", "la ragazze"), "le ragazze", "Femenino plural: le ragazze."),
                    Question("it_a2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Loro non ___ il caffè.'", null, "bevono", listOf("bevono", "beve", "beviamo", "bevi"), "bevono", "Tercera persona plural de bere: bevono."),
                    Question("it_a2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Completa con el adjetivo posesivo: 'Questo è il ___ libro.' (de mí)", null, "mio", listOf("mio", "tuo", "suo", "nostro"), "mio", "'Il mio libro' (mi libro)."),
                    Question("it_a2_5", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice '¿A qué hora?'?", null, "A che ora?", listOf("A che ora?", "Quando ora?", "Che tempo?", "Dove ora?"), "A che ora?", "Fórmula estándar: A che ora?")
                )
                "B1" -> listOf(
                    Question("it_b1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Ieri sera loro ___ al cinema.'", null, "sono andati", listOf("sono andati", "hanno andato", "vanno", "erano andati"), "sono andati", "Andare usa essere en passato prossimo."),
                    Question("it_b1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Completa con imperfetto: 'Da bambino ___ molto.'", null, "leggevo", listOf("leggevo", "ho letto", "leggeva", "leggo"), "leggevo", "Hábito en el pasado: leggevo."),
                    Question("it_b1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Mentre cucinavo, il telefono ___.'", null, "ha squillato", listOf("ha squillato", "squillava", "squilla", "è squillato"), "ha squillato", "Acción puntual que interrumpe: passato prossimo."),
                    Question("it_b1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Traduce 'Hace tres días':", null, "Tre giorni fa", listOf("Tre giorni fa", "Da tre giorni", "In tre giorni", "Fra tre giorni"), "Tre giorni fa", "'Fa' indica tiempo transcurrido en el pasado."),
                    Question("it_b1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Spero che tu ___ bene.'", null, "stia", listOf("stia", "stai", "stavi", "starai"), "stia", "Sperare che rige congiuntivo presente: stia.")
                )
                "B2" -> listOf(
                    Question("it_b2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Periodo ipotetico: 'Se ieri avessi studiato, ___ l'esame.'", null, "avrei passato", listOf("avrei passato", "avessi passato", "passerei", "ho passato"), "avrei passato", "Periodo ipotetico dell'irrealtà: congiuntivo trapassato + condizionale composto."),
                    Question("it_b2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Congiuntivo imperfetto: 'Credevo che voi ___ a casa.'", null, "foste", listOf("foste", "siate", "eravate", "sarete"), "foste", "Concordanza dei tempi al passato con verbo di opinione: foste."),
                    Question("it_b2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Pronomi combinati: 'Mi presti la penna? Sí, ___ presto subito.'", null, "te la", listOf("te la", "ti la", "gliela", "me la"), "te la", "La combinazione di 'a te' (ti) + 'la' diventa 'te la'."),
                    Question("it_b2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Forma passiva: 'In Italia la pizza ___ servita calda.'", null, "viene", listOf("viene", "va", "sta", "fa"), "viene", "Venire si usa come ausiliare passivo nei tempi semplici per indicare azioni in corso."),
                    Question("it_b2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Connettore: '___ facesse freddo, abbiamo fatto una passeggiata.'", null, "Benché", listOf("Benché", "Anche", "Perché", "Quindi"), "Benché", "'Benché' introduce una subordinata concessiva con congiuntivo imperfetto.")
                )
                "C1" -> listOf(
                    Question("it_c1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Congiuntivo trapassato: 'Benché ___ tardi, riuscimmo ad entrare.'", null, "fosse stato", listOf("fosse stato", "era stato", "sia stato", "sarà stato"), "fosse stato", "Azione passata anteriore con congiuntivo trapassato."),
                    Question("it_c1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Modo di dire: 'Gettare la spugna' significa:", null, "Rendersi e abbandonare l'impresa", listOf("Rendersi e abbandonare l'impresa", "Pulire a fondo", "Iniziare una sfida", "Perdere la pazienza"), "Rendersi e abbandonare l'impresa", "Espressione metaforica che indica abbandono definitivo."),
                    Question("it_c1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Forma deontica: 'Questo documento ___ conservato con cura.'", null, "va", listOf("va", "viene", "sta", "può"), "va", "'Andare' + participio passato esprime obbligo o necessità normativa."),
                    Question("it_c1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Sinonimo colto di 'imprescindibile':", null, "Indispensabile", listOf("Indispensabile", "Superfluo", "Opzionale", "Marginale"), "Indispensabile", "'Imprescindibile' denota qualcosa di fondamentale e non rinunciabile."),
                    Question("it_c1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Gerundio composto: '___ le istruzioni, montò l'apparecchio.'", null, "Avendo letto", listOf("Avendo letto", "Leggendo", "Essendo letto", "Avuto letto"), "Avendo letto", "Il gerundio composto esprime anteriorità causale o temporale.")
                )
                else -> listOf(
                    Question("it_c2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Registro letterario: '___ lo avessero avvertito, proseguì.'", null, "Quantunque", listOf("Quantunque", "Poiché", "Onde", "Sicché"), "Quantunque", "'Quantunque' è congiunzione concessiva aulica che regge il congiuntivo."),
                    Question("it_c2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Modo di dire colto: 'Mettere i puntini sulle i' indica:", null, "Precisare meticolosamente i dettagli", listOf("Precisare meticolosamente i dettagli", "Scrivere una bozza", "Riconoscere un errore", "Tralasciare il superfluo"), "Precisare meticolosamente i dettagli", "Indica rigore e puntualizzazione senza lasciare ambiguità."),
                    Question("it_c2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Morfologia verbale d'élite: Participio passato di 'redimere':", null, "redento", listOf("redento", "redimuto", "redesso", "redinto"), "redento", "Forma dotta irregolare: redento."),
                    Question("it_c2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Registro formale/giuridico: La clausola si applica 'in deroga a':", null, "In eccezione a quanto precedentemente stabilito", listOf("In eccezione a quanto precedentemente stabilito", "In totale conformità a", "A prescindere dalla legge", "In attesa di ratifica"), "In eccezione a quanto precedentemente stabilito", "'In deroga a' stabilisce una precisa eccezione a una norma generale."),
                    Question("it_c2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Retorica e stile: Un'argomentazione 'capziosa' è:", null, "Fallace e tesa a trarre in inganno", listOf("Fallace e tesa a trarre in inganno", "Chiara e inconfutabile", "Breve e incisiva", "Priva di interesse"), "Fallace e tesa a trarre in inganno", "'Capzioso' definisce un discorso apparentemente valido ma sostanzialmente ingannevole.")
                )
            }
            "zh" -> when (stage) {
                "A1" -> listOf(
                    Question("zh_a1_1", "diag", QuestionType.MULTIPLE_CHOICE, "¿Qué significa '你好' (Nǐ hǎo)?", "Nǐ hǎo", "你好", listOf("Hola", "Adiós", "Gracias", "Disculpa"), "Hola", "'你好' es el saludo estándar Hola."),
                    Question("zh_a1_2", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice 'Gracias' en mandarín?", "Xièxie", "谢谢", listOf("谢谢 (Xièxie)", "再见 (Zàijiàn)", "对不起 (Duìbuqǐ)", "没关系 (Méiguānxi)"), "谢谢 (Xièxie)", "谢谢 significa gracias."),
                    Question("zh_a1_3", "diag", QuestionType.MULTIPLE_CHOICE, "¿Qué significa '再见' (Zàijiàn)?", "Zàijiàn", "再见", listOf("Adiós", "Hola", "Por favor", "Sí"), "Adiós", "'再见' literalmente es verte de nuevo (adiós)."),
                    Question("zh_a1_4", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice 'Yo' en chino?", "Wǒ", "我", listOf("我 (Wǒ)", "你 (Nǐ)", "他 (Tā)", "我们 (Wǒmen)"), "我 (Wǒ)", "'我' es Yo."),
                    Question("zh_a1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Partícula interrogativa para preguntas de sí/no:", "Ma", "吗", listOf("吗 (ma)", "呢 (ne)", "吧 (ba)", "的 (de)"), "吗 (ma)", "'吗' convierte oraciones en preguntas.")
                )
                "A2" -> listOf(
                    Question("zh_a2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Selecciona el clasificador general: '一___人'", "Yī gè rén", "一个人", listOf("个 (gè)", "本 (běn)", "只 (zhī)", "张 (zhāng)"), "个 (gè)", "'个' es el clasificador general."),
                    Question("zh_a2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Traduce 'Tengo 20 años':", "Wǒ èrshí suì", "我二十岁", listOf("我二十岁", "我二十年", "我是二十岁", "我有二十岁"), "我二十岁", "La edad se expresa directamente con 岁 (suì)."),
                    Question("zh_a2_3", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice 'Comer comida'?", "Chīfàn", "吃饭", listOf("吃饭 (chīfàn)", "喝水 (hēshuǐ)", "睡觉 (shuìjiào)", "去学校 (qù xuéxiào)"), "吃饭 (chīfàn)", "吃饭 es comer."),
                    Question("zh_a2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Palabra para negar en presente/futuro:", "Bù", "不", listOf("不 (bù)", "没 (méi)", "别 (bié)", "无 (wú)"), "不 (bù)", "'不' es la negación general."),
                    Question("zh_a2_5", "diag", QuestionType.MULTIPLE_CHOICE, "¿Qué significa '多少钱' (Duōshǎo qián)?", "Duōshǎo qián", "多少钱", listOf("¿Cuánto cuesta?", "¿Dónde está?", "¿Quién es?", "¿Qué hora es?"), "¿Cuánto cuesta?", "Pregunta por precio.")
                )
                "B1" -> listOf(
                    Question("zh_b1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Partícula de acción completada: '我买___一本词典。'", "Wǒ mǎi le yì běn cídiǎn", "我买了一本词典", listOf("了 (le)", "过 (guo)", "着 (zhe)", "在 (zài)"), "了 (le)", "'了' indica aspecto completado."),
                    Question("zh_b1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Conector de causa: '因为今天下雨，___我们没去。'", "Yīnwèi... suǒyǐ", "因为所以", listOf("所以 (suǒyǐ)", "但是 (dànshì)", "虽然 (suīrán)", "如果 (rúguǒ)"), "所以 (suǒyǐ)", "Pareja Porque... por eso."),
                    Question("zh_b1_3", "diag", QuestionType.MULTIPLE_CHOICE, "¿Qué indica '正在' (zhèngzài) en '他正在看书'?", "Zhèngzài", "正在", listOf("Acción en progreso continuo (está leyendo)", "Acción pasada", "Obligación", "Deseo"), "Acción en progreso continuo (está leyendo)", "'正在' equivale a estar + gerundio."),
                    Question("zh_b1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Experiencia previa: '我___中国，风景很美。'", "Wǒ qùguo Zhōngguó", "我去过中国", listOf("去过 (qùguo)", "去了 (qùle)", "在去 (zài qù)", "会去 (huì qù)"), "去过 (qùguo)", "'过' enfatiza haber tenido la experiencia en el pasado."),
                    Question("zh_b1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Conector adversativo: '虽然汉语很难，___很有趣。'", "Suīrán... dànshì", "虽然但是", listOf("但是 (dànshì)", "所以 (suǒyǐ)", "因为 (yīnwèi)", "或者 (huòzhě)"), "促进 (dànshì)", "Correlativo: Aunque... pero.")
                )
                "B2" -> listOf(
                    Question("zh_b2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Estructura 把: '请你把桌上的书___。'", "Qǐng nǐ bǎ zhuō shàng de shū shōu qǐlái", "请你把桌上的书收起来", listOf("收起来 (shōu qǐlái)", "看 (kàn)", "书 (shū)", "在桌上 (zài zhuō shàng)"), "收起来 (shōu qǐlái)", "La estructura 把 requiere complemento de resultado."),
                    Question("zh_b2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Voz pasiva: '蛋糕被弟弟___了。'", "Dàngāo bèi dìdi chī diào le", "蛋糕被弟弟吃掉了", listOf("吃掉 (chī diào)", "吃 (chī)", "买 (mǎi)", "看 (kàn)"), "吃掉 (chī diào)", "Estructura pasiva con 被 y resultado 掉."),
                    Question("zh_b2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Correlativo aditivo: '他___会英语，而且会法语。'", "Bùjǐn... érqiě", "不仅而且", listOf("不仅 (bùjǐn)", "如果 (rúguǒ)", "因为 (yīnwèi)", "虽然 (suīrán)"), "不仅 (bùjǐn)", "No solo... sino también: 不仅...而且."),
                    Question("zh_b2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Condición: '___明天下雨，比赛都照常进行。'", "Wúlùn míngtiān xiàyǔ, dōu...", "无论明天下雨都", listOf("无论 (wúlùn)", "只有 (zhǐyǒu)", "只要 (zhǐyào)", "如果 (rúguǒ)"), "无论 (wúlùn)", "无论...都 denota 'sin importar que'."),
                    Question("zh_b2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Progresión: '天气___冷了。'", "Tiānqì yuè lái yuè lěng le", "天气越来越冷了", listOf("越来越 (yuè lái yuè)", "更 (gèng)", "非常 (fēicháng)", "极了 (jíle)"), "越来越 (yuè lái yuè)", "越来越 indica incremento progresivo.")
                )
                "C1" -> listOf(
                    Question("zh_c1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Chengyu: Significado de '半途而废' (bàn tú ér fèi):", "Bàn tú ér fèi", "半途而废", listOf("Abandonar o rendirse a mitad de camino", "Tener éxito rápidamente", "Viajar con amigos", "Caminar despacio"), "Abandonar o rendirse a mitad de camino", "半途 (medio camino) 而废 (abandonar)."),
                    Question("zh_c1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Enfático de sorpresa: '他___在三天内完成了任务！'", "Tā jūrán...", "他居然", listOf("居然 (jūrán)", "果然 (guǒrán)", "既然 (jìrán)", "突然 (tūrán)"), "居然 (jūrán)", "居然 denota sorpresa contra lo esperado."),
                    Question("zh_c1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Preferencia: '与其抱怨，___努力改变。'", "Yǔqí... bùrú", "与其不如", listOf("不如 (bùrú)", "而是 (érshì)", "虽然 (suīrán)", "因此 (yīncǐ)"), "不如 (bùrú)", "En vez de A, más vale B: 与其...不如."),
                    Question("zh_c1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Causa evidente: '难怪他这么高兴，___他通过了考试。'", "Nánguài... yuánlái", "难怪原来", listOf("原来 (yuánlái)", "本来 (běnlái)", "如果 (rúguǒ)", "虽然 (suīrán)"), "原来 (yuánlái)", "Con razón... resulta que: 难怪...原来."),
                    Question("zh_c1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Negación formal escrita: '这件事与他___关系。'", "Háo wú guānxì", "毫无关系", listOf("毫无 (háo wú)", "没有 (méi yǒu)", "不 (bù)", "无须 (wú xū)"), "毫无 (háo wú)", "毫无 significa carecer en lo absoluto (registro escrito).")
                )
                else -> listOf(
                    Question("zh_c2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Chengyu clásico: ¿Qué denota '未雨绸缪' (wèi yǔ chóu móu)?", "Wèi yǔ chóu móu", "未雨绸缪", listOf("Tomar precauciones antes de una dificultad", "Esperar que llueva", "Reparar la casa", "Lamentarse"), "Tomar precauciones antes de una dificultad", "Prepararse con anticipación antes de la tempestad."),
                    Question("zh_c2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Chino clásico (文言文): '赤子___心' (Chìzǐ zhī xīn)", "Chìzǐ zhī xīn", "赤子之心", listOf("之 (zhī)", "的 (de)", "得 (de)", "地 (de)"), "之 (zhī)", "之 cumple la función culta de genitivo/posesivo."),
                    Question("zh_c2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Registro diplomático/académico: '___目前严峻形势，双方深化合作。'", "Jiànyú...", "鉴于", listOf("鉴于 (jiànyú)", "关于 (guānyú)", "至于 (zhìyú)", "对于 (duìyú)"), "鉴于 (jiànyú)", "鉴于 significa 'En vista de / Considerando'."),
                    Question("zh_c2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Partícula clásica: '敏而好学，不耻下问，是___谓之文也。'", "Shì yǐ...", "是以", listOf("以 (yǐ)", "于 (yú)", "之 (zhī)", "而 (ér)"), "以 (yǐ)", "是以 equivale a 'por lo tanto / por eso' en chino clásico."),
                    Question("zh_c2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Metáfora filosófica: ¿Qué critica '刻舟求剑' (kè zhōu qiú jiàn)?", "Kè zhōu qiú jiàn", "刻舟求剑", listOf("La terquedad de usar métodos caducos sin adaptarse al cambio", "Navegar con armas", "Perder cosas en el río", "El paso del tiempo"), "La terquedad de usar métodos caducos sin adaptarse al cambio", "Parábola clásica sobre la incapacidad de adaptarse a las circunstancias cambiantes.")
                )
            }
            "pt" -> when (stage) {
                "A1" -> listOf(
                    Question("pt_a1_1", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice 'Buenos días, ¿cómo está usted?' en portugués?", null, "Bom dia, tudo bem com você?", listOf("Bom dia, tudo bem com você?", "Boa noite, até amanhã", "Com licença, por favor", "Muito obrigado pela ajuda"), "Bom dia, tudo bem com você?", "'Bom dia, tudo bem com você?' es el saludo matutino cordial."),
                    Question("pt_a1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Traduce 'Muchas gracias por tu ayuda hoy':", null, "Muito obrigado pela sua ajuda hoje.", listOf("Muito obrigado pela sua ajuda hoje.", "Por favor, onde fica a estação?", "Prazer em conhecer você.", "Eu estudo português todos os dias."), "Muito obrigado pela sua ajuda hoje.", "'Muito obrigado pela sua ajuda hoje' expresa agradecimiento completo."),
                    Question("pt_a1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Eu ___ português todos os dias.'", null, "estudo", listOf("estudo", "estuda", "estudamos", "estudam"), "estudo", "Primera persona singular de estudar: Eu estudo."),
                    Question("pt_a1_4", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se pide un café cortésmente?", null, "Eu gostaria de um café com leite, por favor.", listOf("Eu gostaria de um café com leite, por favor.", "Eu não falo português.", "A conta, por gentileza.", "Onde fica o hotel?"), "Eu gostaria de um café com leite, por favor.", "'Eu gostaria de...' equivale a 'Me gustaría...'."),
                    Question("pt_a1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Muito prazer em ___ você.'", null, "conhecer", listOf("conhecer", "falar", "comer", "morar"), "conhecer", "'Prazer em conhecer você' significa mucho gusto en conocerte.")
                )
                "A2" -> listOf(
                    Question("pt_a2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Completa en pasado: 'Ontem nós ___ um museu histórico.'", null, "visitamos", listOf("visitamos", "visitaram", "visito", "visitarei"), "visitamos", "Pretérito perfeito con nós: visitamos."),
                    Question("pt_a2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Ela ___ de acordar cedo todos os dias.'", null, "gosta", listOf("gosta", "gosto", "gostam", "gostamos"), "gosta", "El verbo gostar rige la preposición 'de': Ela gosta de..."),
                    Question("pt_a2_3", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo preguntas cuánto cuesta un pasaje?", null, "Quanto custa a passagem de trem?", listOf("Quanto custa a passagem de trem?", "Que horas são agora?", "Onde você mora?", "Como está o tempo hoje?"), "Quanto custa a passagem de trem?", "'Quanto custa...?' pregunta por el precio."),
                    Question("pt_a2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Completa con pretérito imperfeito: 'Quando eu era criança, ___ na praia.'", null, "brincava", listOf("brincava", "brinquei", "brinco", "brincarei"), "brincava", "Hábito en la infancia: pretérito imperfeito (brincava)."),
                    Question("pt_a2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Eles estão ___ para a prova agora.'", null, "estudando", listOf("estudando", "estudar", "estudado", "estudam"), "estudando", "En portugués brasileño el gerundio se forma con -ndo: estudando.")
                )
                "B1" -> listOf(
                    Question("pt_b1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Subjuntivo presente: 'Espero que você ___ uma ótima viagem.'", null, "tenha", listOf("tenha", "tem", "teve", "teria"), "tenha", "Esperar que exige presente do subjuntivo: tenha."),
                    Question("pt_b1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Futuro do subjuntivo: 'Quando nós ___ em Lisboa, ligaremos.'", null, "chegarmos", listOf("chegarmos", "chegamos", "cheguemos", "chegar"), "chegarmos", "Quando referido al futuro usa futuro do subjuntivo: chegarmos."),
                    Question("pt_b1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Condicional: 'Se você tiver tempo, ___ me ajudar?'", null, "poderia", listOf("poderia", "pode", "pôde", "poderá"), "poderia", "Futuro do pretérito de cortesía: poderia."),
                    Question("pt_b1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Conector concesivo: '___ estivesse chovendo, fomos caminhar.'", null, "Embora", listOf("Embora", "Porque", "Portanto", "Quando"), "Embora", "'Embora' significa 'aunque' y exige subjuntivo."),
                    Question("pt_b1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Faz dois anos que ___ nesta empresa.'", null, "trabalho", listOf("trabalho", "trabalhava", "trabalharei", "trabalhe"), "trabalho", "Fazer indicando tiempo transcurrido se usa con presente: trabalho.")
                )
                "B2" -> listOf(
                    Question("pt_b2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Condicional hipotético: 'Se eu soubesse antes, ___ uma solução.'", null, "teria proposto", listOf("teria proposto", "tinha proposto", "proporia", "propus"), "teria proposto", "Condicional compuesto para hipótesis pasada: teria proposto."),
                    Question("pt_b2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Infinitivo pessoal: 'É fundamental ___ nossas metas com clareza.'", null, "definirmos", listOf("definirmos", "definir", "definimos", "definindo"), "definirmos", "El infinitivo pessoal en portugués flexiona la persona (nós -> definirmos)."),
                    Question("pt_b2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Voz pasiva: 'O relatório foi ___ pela diretoria.'", null, "aprovado", listOf("aprovado", "aprovar", "aprovando", "aprovou"), "aprovado", "Voz pasiva analítica: foi + participio pasado (aprovado)."),
                    Question("pt_b2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Subjuntivo imperfecto: 'Caso eles ___ mais prazo, avisariam.'", null, "precisassem", listOf("precisassem", "precisam", "precisarem", "precisavam"), "precisassem", "La conjunción 'Caso' rige pretérito imperfeito do subjuntivo."),
                    Question("pt_b2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Conector consecutivo: 'Os dados foram validados; ___, podemos avançar.'", null, "portanto", listOf("portanto", "embora", "contudo", "todavia"), "portanto", "'Portanto' significa 'por lo tanto'.")
                )
                "C1" -> listOf(
                    Question("pt_c1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Conector culto: '___ os obstáculos financeiros, o projeto prosperou.'", null, "Não obstante", listOf("Não obstante", "Porquanto", "Portanto", "Conquanto que"), "Não obstante", "'Não obstante' equivale a 'no obstante / a pesar de'."),
                    Question("pt_c1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Pronombre relativo con preposición: 'A tese ___ qual o autor se apoia é sólida.'", null, "na", listOf("na", "que", "cujo", "onde"), "na", "Apoiar-se em + a qual = na qual."),
                    Question("pt_c1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Léxico C1: Algo que es 'imprescindível' significa:", null, "Indispensável e fundamental", listOf("Indispensável e fundamental", "Opcional e secundário", "Duvidoso e incerto", "Temporário e breve"), "Indispensável e fundamental", "'Imprescindível' denota algo de lo que no se puede prescindir."),
                    Question("pt_c1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Subjuntivo mais-que-perfeito: 'Se nós ___ previsto o risco, teríamos evitado.'", null, "tivéssemos", listOf("tivéssemos", "tínhamos", "teríamos", "tivermos"), "tivéssemos", "Mais-que-perfeito do subjuntivo: tivéssemos previsto."),
                    Question("pt_c1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Expresión idiomática: 'Trocar os pés pelas mãos' significa:", null, "Atrapalhar-se e agir com precipitação", listOf("Atrapalhar-se e agir com precipitação", "Caminhar rapidamente", "Mudar de opinião com calma", "Dançar com habilidade"), "Atrapalhar-se e agir com precipitação", "Modismo clásico que indica confundirse por actuar apresuradamente.")
                )
                else -> listOf(
                    Question("pt_c2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Mesóclisis literaria: '___-se-ia que tal hipótese carece de fundamento.'", null, "Dir", listOf("Dir", "Dizer", "Disse", "Direi"), "Dir", "Mesóclisis culta en futuro do pretérito: Dir-se-ia (Se diría)."),
                    Question("pt_c2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Conector causal erudito: 'A decisão é legítima, ___ respeita a Constituição.'", null, "porquanto", listOf("porquanto", "conquanto", "contudo", "outrossim"), "porquanto", "'Porquanto' es una conjunción causal culta equivalente a 'puesto que / ya que'."),
                    Question("pt_c2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Retórica C2: Uma argumentação 'capciosa' caracteriza-se por ser:", null, "Enganosa e formulada para induzir ao erro", listOf("Enganosa e formulada para induzir ao erro", "Cristalina e irrefutável", "Concisa e imparcial", "Superficial e ingênua"), "Enganosa e formulada para induzir ao erro", "'Capcioso' describe un argumento falaz pero aparentemente convincente."),
                    Question("pt_c2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Adverbio aditivo formal: 'Cumpre destacar, ___, a relevância jurídica do tratado.'", null, "outrossim", listOf("outrossim", "todavia", "destarte", "amiúde"), "outrossim", "'Outrossim' significa 'asimismo / igualmente' en registro jurídico y académico."),
                    Question("pt_c2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Modismo erudito: 'Passar incólume por uma crise' significa:", null, "Sair ileso e sem sofrer danos", listOf("Sair ileso e sem sofrer danos", "Sofrer grandes prejuízos", "Ignorar os problemas alheios", "Adiar uma decisão urgente"), "Sair ileso e sem sofrer danos", "'Incólume' significa ileso, intacto o libre de daño.")
                )
            }
            else -> when (stage) {
                "A1" -> listOf(
                    Question("en_a1_1", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se saluda en la mañana en inglés?", null, "Good morning", listOf("Good morning", "Good night", "See you later", "Goodbye"), "Good morning", "'Good morning' es buenos días."),
                    Question("en_a1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Traduce 'Por favor':", null, "Please", listOf("Please", "Thank you", "Sorry", "Welcome"), "Please", "'Please' es por favor."),
                    Question("en_a1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'I ___ a student.'", null, "am", listOf("am", "is", "are", "be"), "am", "Con 'I' se usa 'am'."),
                    Question("en_a1_4", "diag", QuestionType.MULTIPLE_CHOICE, "¿Cómo se dice 'Gracias'?", null, "Thank you", listOf("Thank you", "Hello", "Welcome", "Excuse me"), "Thank you", "'Thank you' es gracias."),
                    Question("en_a1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Nice to ___ you.'", null, "meet", listOf("meet", "meat", "meeting", "see"), "meet", "Frase de cortesía: Nice to meet you.")
                )
                "A2" -> listOf(
                    Question("en_a2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'She ___ coffee every morning.'", null, "drinks", listOf("drinks", "drink", "drinking", "drank"), "drinks", "Con he/she/it en presente simple se añade '-s'."),
                    Question("en_a2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'They ___ in a big apartment.'", null, "live", listOf("live", "lives", "living", "are live"), "live", "Con 'They' el verbo va en forma base."),
                    Question("en_a2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Where ___ you yesterday?'", null, "were", listOf("were", "was", "are", "been"), "were", "Pasado del verbo to be con 'you': were."),
                    Question("en_a2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'He doesn't ___ spicy food.'", null, "like", listOf("like", "likes", "liking", "liked"), "like", "Tras el auxiliar doesn't el verbo va en forma base."),
                    Question("en_a2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'We are ___ dinner right now.'", null, "cooking", listOf("cooking", "cook", "cooked", "cooks"), "cooking", "Presente continuo: are + cooking.")
                )
                "B1" -> listOf(
                    Question("en_b1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Pasado irregular: 'Yesterday they ___ a movie.'", null, "saw", listOf("saw", "see", "seen", "seeing"), "saw", "El pasado de see es saw."),
                    Question("en_b1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'I have lived here ___ 2020.'", null, "since", listOf("since", "for", "during", "ago"), "since", "'Since' se usa con un punto de inicio específico."),
                    Question("en_b1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Primer condicional: 'If it rains, we ___ at home.'", null, "will stay", listOf("will stay", "stay", "stayed", "would stay"), "will stay", "Primer condicional: If + presente, will + verbo."),
                    Question("en_b1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Hábito pasado: 'He used to ___ football.'", null, "play", listOf("play", "playing", "played", "plays"), "play", "'Used to' va seguido de verbo en infinitivo."),
                    Question("en_b1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Voz pasiva: 'This house was ___ in 1995.'", null, "built", listOf("built", "build", "building", "builts"), "built", "Participio pasado de build: built.")
                )
                "B2" -> listOf(
                    Question("en_b2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Segundo condicional: 'If I had money, I ___ travel.'", null, "would", listOf("would", "will", "can", "am"), "would", "Segundo condicional hipotético: would + verbo base."),
                    Question("en_b2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Futuro perfecto: 'By next year, she ___ graduated.'", null, "will have", listOf("will have", "will", "has", "would have"), "will have", "Futuro perfecto: will have + participio."),
                    Question("en_b2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Completa: 'Despite ___ tired, he worked.'", null, "being", listOf("being", "be", "was", "been"), "being", "Tras preposiciones como 'despite' se usa gerundio -ing."),
                    Question("en_b2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Deseo pasado: 'I wish I ___ studied harder.'", null, "had", listOf("had", "have", "would", "did"), "had", "Regret pasado: wish + past perfect (had)."),
                    Question("en_b2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Modal de reproche: 'You should have ___ me.'", null, "told", listOf("told", "tell", "telling", "tells"), "told", "Should have + past participle: told.")
                )
                "C1" -> listOf(
                    Question("en_c1_1", "diag", QuestionType.MULTIPLE_CHOICE, "Synonym for 'meticulous':", null, "Thorough", listOf("Thorough", "Careless", "Simple", "Speedy"), "Thorough", "'Thorough' significa exhaustivo y meticuloso."),
                    Question("en_c1_2", "diag", QuestionType.MULTIPLE_CHOICE, "Idiom: 'Bite the bullet' means:", null, null, listOf("Afrontar una situación difícil con valor", "Comer algo duro", "Rendirse", "Disparar"), "Afrontar una situación difícil con valor", "Afrontar lo inevitable."),
                    Question("en_c1_3", "diag", QuestionType.MULTIPLE_CHOICE, "Inversion: 'Seldom ___ such dedication.'", null, "have I seen", listOf("have I seen", "I have seen", "did I saw", "saw I"), "have I seen", "Tras adverbio negativo: auxiliar + sujeto."),
                    Question("en_c1_4", "diag", QuestionType.MULTIPLE_CHOICE, "Preposition: 'He is prone ___ making errors.'", null, "to", listOf("to", "for", "with", "at"), "to", "Prone se construye con 'to'."),
                    Question("en_c1_5", "diag", QuestionType.MULTIPLE_CHOICE, "Nuance: The plan was deemed ___.", null, "feasible", listOf("feasible", "feasibly", "feasibility", "feasibleness"), "feasible", "Adjetivo predicativo: feasible (viable).")
                )
                else -> listOf(
                    Question("en_c2_1", "diag", QuestionType.MULTIPLE_CHOICE, "Inversion: 'Hardly ___ entered when it rang.'", null, "had he", listOf("had he", "he had", "did he", "has he"), "had he", "Hardly + had + sujeto."),
                    Question("en_c2_2", "diag", QuestionType.MULTIPLE_CHOICE, "Condition: 'Were it not ___ your help, I would fail.'", null, "for", listOf("for", "by", "with", "to"), "for", "Estructura formal: Were it not for..."),
                    Question("en_c2_3", "diag", QuestionType.MULTIPLE_CHOICE, "Nuance: The speaker made a ___ reference.", null, "subtle", listOf("subtle", "subtly", "subtlety", "subtleness"), "subtle", "Adjetivo para matiz sutil: subtle."),
                    Question("en_c2_4", "diag", QuestionType.MULTIPLE_CHOICE, "Correlative: 'No sooner had we left ___ it rained.'", null, "than", listOf("than", "when", "then", "that"), "than", "No sooner va acompañado de 'than'."),
                    Question("en_c2_5", "diag", QuestionType.MULTIPLE_CHOICE, "Advanced lexicon: An argument that seems plausible but is false is:", null, "specious", listOf("specious", "cogent", "lucid", "veracious"), "specious", "'Specious' significa falaz o engañoso.")
                )
            }
        }
    }

    val initialStageIdx = stages.indexOf(currentAssignedCefr.take(2).uppercase()).coerceAtLeast(0)
    var stageIndex by remember { mutableStateOf(initialStageIdx) } // 0 = A1, 1 = A2, etc.
    var questionInStageIndex by remember { mutableStateOf(0) } // 0 to 4 (5 preguntas por etapa)
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var answerResult by remember { mutableStateOf<AnswerResult?>(null) }
    var stageCorrectCount by remember { mutableStateOf(0) }
    var totalCorrectCount by remember { mutableStateOf(0) }
    var showStageAdvanceDialog by remember { mutableStateOf(false) }
    var showFinalResultDialog by remember { mutableStateOf(false) }

    val evaluateUseCase = remember { EvaluateAnswerUseCase() }

    val currentCefrCode = stages.getOrElse(stageIndex) { "A1" }
    val stageQuestions = remember(languageId, stageIndex) {
        getQuestionsForStage(languageId, currentCefrCode)
    }
    val currentQuestion = stageQuestions.getOrNull(questionInStageIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Diagnóstico Adaptativo MCER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Evaluando: Nivel $currentCefrCode | Idioma: $languageName",
                            fontSize = 13.sp,
                            color = PrimaryGreenDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoBackground,
                    titleContentColor = TextLight,
                    navigationIconContentColor = TextLight
                )
            )
        },
        containerColor = IndigoBackground,
        bottomBar = {
            if (answerResult != null) {
                val result = answerResult!!
                val isSuccess = result.isCorrect

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSuccess) SuccessGreenLight else ErrorRedLight)
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isSuccess) "✅ ¡RESPUESTA CORRECTA!" else "❌ RESPUESTA INCORRECTA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) PrimaryGreenDark else ErrorRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!isSuccess) {
                        Text(
                            text = "Solución correcta: ${result.correctAnswer}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "💡 ${result.explanation}",
                            fontSize = 13.sp,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (result.isCorrect) {
                                stageCorrectCount++
                                totalCorrectCount++
                            }
                            answerResult = null
                            selectedOption = null

                            if (questionInStageIndex + 1 < stageQuestions.size) {
                                questionInStageIndex++
                            } else {
                                // Fin de las 5 preguntas del nivel actual: Verificar criterio del 80% (mínimo 4 aciertos)
                                val passedStage = stageCorrectCount >= 4

                                if (passedStage && stageIndex + 1 < stages.size) {
                                    // Superó el nivel: mostrar transición y avanzar al siguiente nivel
                                    showStageAdvanceDialog = true
                                } else {
                                    // No alcanzó el 80% o llegó al nivel máximo (C2): Finalizar prueba
                                    showFinalResultDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSuccess) PrimaryGreen else ErrorRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val isLastInStage = questionInStageIndex == stageQuestions.size - 1
                        Text(
                            if (isLastInStage) "EVALUAR BLOQUE $currentCefrCode" else "SIGUIENTE PREGUNTA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (currentQuestion != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = {
                            selectedOption?.let {
                                answerResult = evaluateUseCase.execute(currentQuestion, it)
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = LockedGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("COMPROBAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                val currentLevelIdx = stages.indexOf(currentAssignedCefr.take(2).uppercase()).coerceAtLeast(0)
                val maxSelectableStageIdx = maxOf(currentLevelIdx, stageIndex)

                // Selector de etapas MCER para saltar o practicar niveles ya desbloqueados
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stages.forEachIndexed { idx, stageName ->
                        val isAllowed = idx <= maxSelectableStageIdx
                        val isCurrent = idx == stageIndex
                        FilterChip(
                            selected = isCurrent,
                            onClick = {
                                if (isAllowed && idx != stageIndex) {
                                    stageIndex = idx
                                    questionInStageIndex = 0
                                    selectedOption = null
                                    answerResult = null
                                    stageCorrectCount = 0
                                }
                            },
                            enabled = isAllowed,
                            label = {
                                Text(
                                    text = if (isAllowed) stageName else "$stageName 🔒",
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryGreenDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (questionInStageIndex.toFloat() + 1) / stageQuestions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = PrimaryGreen,
                    trackColor = CardBorder
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pregunta ${questionInStageIndex + 1} de 5 en $currentCefrCode",
                        color = TextGray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Aciertos: $stageCorrectCount/5 (Necesitas 4 para avanzar)",
                        color = PrimaryGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = currentQuestion.prompt,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (!currentQuestion.promptPhonetic.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pinyin: ${currentQuestion.promptPhonetic}",
                        fontSize = 16.sp,
                        color = PrimaryGreenDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                currentQuestion.options.forEach { option ->
                    val isSelected = selectedOption == option
                    val borderColor = if (isSelected) PrimaryCoral else CardBorder
                    val bgColor = if (isSelected) PrimaryCoral.copy(alpha = 0.2f) else IndigoCard

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable(enabled = answerResult == null) {
                                selectedOption = option
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = if (isSelected) 2.dp else 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCoral)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextLight
                            )
                        }
                    }
                }
            }
        }

        // Diálogo Intermedio: ¡Superó la etapa y avanza al siguiente nivel!
        if (showStageAdvanceDialog) {
            val nextCefr = stages.getOrNull(stageIndex + 1) ?: "A2"
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Aprobado",
                        tint = AccentYellow,
                        modifier = Modifier.size(52.dp)
                    )
                },
                title = {
                    Text("🎉 ¡Bloque $currentCefrCode Superado!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                text = {
                    Column {
                        Text(
                            text = "Obtuviste $stageCorrectCount de 5 aciertos (${(stageCorrectCount * 100) / 5}%).",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = PrimaryGreenDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Has demostrado dominar el nivel $currentCefrCode. Ahora evaluaremos si tienes conocimientos para convalidar también el nivel $nextCefr.",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showStageAdvanceDialog = false
                            stageIndex++
                            questionInStageIndex = 0
                            stageCorrectCount = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("EVALUAR NIVEL $nextCefr", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showStageAdvanceDialog = false
                            val nextStageOrder = (stageIndex + 1).coerceAtMost(stages.size - 1)
                            val targetCefr = stages[nextStageOrder]
                            onPlacementFinished(nextStageOrder + 1, targetCefr, (stageCorrectCount * 100) / 5)
                        }
                    ) {
                        Text("EMPEZAR A ESTUDIAR EN $nextCefr", fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                    }
                }
            )
        }

        // Diálogo Final: Asignación definitiva del Nivel
        if (showFinalResultDialog) {
            val passedCurrentStage = stageCorrectCount >= 4
            // Si aprobó la etapa actual que rindió (>= 80%), se le asigna el nivel siguiente para estudiar.
            // Si no la aprobó, se le asigna esta etapa para estudiar.
            val assignedStageIndex = if (passedCurrentStage) {
                (stageIndex + 1).coerceAtMost(stages.size - 1)
            } else {
                stageIndex
            }
            val assignedCefr = stages.getOrElse(assignedStageIndex) { "A1" }
            val startingOrder = assignedStageIndex + 1

            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Icon(
                        if (startingOrder > 1) Icons.Default.EmojiEvents else Icons.Default.School,
                        contentDescription = "Resultado Final",
                        tint = if (startingOrder > 1) AccentYellow else PrimaryGreen,
                        modifier = Modifier.size(56.dp)
                    )
                },
                title = {
                    Text("Diagnóstico Finalizado", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                text = {
                    Column {
                        Text(
                            text = "Nivel Asignado: $assignedCefr",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryGreenDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (passedCurrentStage) {
                                "¡Felicitaciones! Superaste el bloque $currentCefrCode con un ${(stageCorrectCount * 100) / 5}% de aciertos. Se han acreditado los conocimientos previos y comenzarás a estudiar directamente en $assignedCefr."
                            } else {
                                "En el bloque $currentCefrCode obtuviste $stageCorrectCount de 5 aciertos. Comenzarás a estudiar en el Nivel $assignedCefr para dominar todas sus competencias antes de avanzar."
                            },
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showFinalResultDialog = false
                            onPlacementFinished(startingOrder, assignedCefr, (stageCorrectCount * 100) / 5)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("EMPEZAR A ESTUDIAR EN $assignedCefr", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
