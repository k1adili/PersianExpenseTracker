package com.rialtracker.expense.util

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * نویسنده‌ی سبک فایل XLSX (فرمت OOXML SpreadsheetML) بدون نیاز به کتابخانه‌ی خارجی.
 * از Apache POI به‌عمد استفاده نشده چون روی اندروید معمولاً باعث مشکلات بیلد/حجم بالا می‌شود؛
 * برای گزارش‌های جدولی ساده (که نیاز این اپ است) این پیاده‌سازی کاملاً کافی و قابل‌اطمینان است.
 * خروجی با هر نسخه‌ای از Excel، Google Sheets و LibreOffice باز می‌شود.
 */
object XlsxWriter {

    sealed class Cell {
        data class Text(val value: String) : Cell()
        data class Number(val value: Double) : Cell()
        companion object {
            fun of(value: String) = Text(value)
            fun of(value: Long) = Number(value.toDouble())
            fun of(value: Int) = Number(value.toDouble())
        }
    }

    class Sheet(val name: String) {
        val rows = mutableListOf<List<Cell>>()
        fun addRow(vararg cells: Cell) {
            rows.add(cells.toList())
        }
        fun addRow(cells: List<Cell>) {
            rows.add(cells)
        }
    }

    private fun xmlEscape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    /** تبدیل اندیس ستون صفر-پایه به حروف اکسل (0 -> A, 1 -> B, 26 -> AA ...) */
    private fun colLetter(index: Int): String {
        var i = index
        val sb = StringBuilder()
        do {
            sb.append(('A' + (i % 26)))
            i = i / 26 - 1
        } while (i >= 0)
        return sb.reverse().toString()
    }

    fun write(out: OutputStream, sheets: List<Sheet>) {
        // نام شیت‌های اکسل حداکثر ۳۱ کاراکترند و نباید کاراکترهای خاصی داشته باشند
        val safeSheets = sheets.map { it.name.take(31) to it }

        ZipOutputStream(out).use { zip ->
            fun entry(path: String, content: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }

            entry(
                "[Content_Types].xml",
                buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
                    append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
                    append("""<Default Extension="xml" ContentType="application/xml"/>""")
                    append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
                    append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
                    safeSheets.forEachIndexed { i, _ ->
                        append("""<Override PartName="/xl/worksheets/sheet${i + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
                    }
                    append("</Types>")
                }
            )

            entry(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>""".trimIndent()
            )

            entry(
                "xl/workbook.xml",
                buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
                    append("<sheets>")
                    safeSheets.forEachIndexed { i, (name, _) ->
                        append("""<sheet name="${xmlEscape(name)}" sheetId="${i + 1}" r:id="rId${i + 1}"/>""")
                    }
                    append("</sheets>")
                    append("</workbook>")
                }
            )

            entry(
                "xl/_rels/workbook.xml.rels",
                buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
                    safeSheets.forEachIndexed { i, _ ->
                        append("""<Relationship Id="rId${i + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${i + 1}.xml"/>""")
                    }
                    append("""<Relationship Id="rIdStyles" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
                    append("</Relationships>")
                }
            )

            entry(
                "xl/styles.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
                <fills count="1"><fill><patternFill patternType="none"/></fill></fills>
                <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
                <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
                </styleSheet>""".trimIndent()
            )

            safeSheets.forEachIndexed { sheetIdx, (_, sheet) ->
                val xml = buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
                    append("""<sheetViews><sheetView rightToLeft="1" workbookViewId="0"/></sheetViews>""")
                    append("<sheetData>")
                    sheet.rows.forEachIndexed { rowIdx, row ->
                        val r = rowIdx + 1
                        append("""<row r="$r">""")
                        row.forEachIndexed { colIdx, cell ->
                            val ref = "${colLetter(colIdx)}$r"
                            when (cell) {
                                is Cell.Text -> append("""<c r="$ref" t="inlineStr"><is><t xml:space="preserve">${xmlEscape(cell.value)}</t></is></c>""")
                                is Cell.Number -> {
                                    val numStr = if (cell.value == Math.floor(cell.value) && !cell.value.isInfinite()) {
                                        cell.value.toLong().toString()
                                    } else cell.value.toString()
                                    append("""<c r="$ref"><v>$numStr</v></c>""")
                                }
                            }
                        }
                        append("</row>")
                    }
                    append("</sheetData>")
                    append("</worksheet>")
                }
                entry("xl/worksheets/sheet${sheetIdx + 1}.xml", xml)
            }
        }
    }
}
