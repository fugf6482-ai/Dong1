package com.dong.app

/**
 * Máy ảo mini: 8 thanh ghi R0..R7, chạy chương trình assembly đơn giản.
 *
 * Lệnh: MOV, ADD, SUB, MUL, PRINT, JMP, JNZ, HALT. Nhãn dạng "ten:". Chú thích bắt đầu bằng ';'.
 */
class Vm {

    fun run(source: String, maxSteps: Int = 100_000): String {
        val out = StringBuilder()
        val labels = HashMap<String, Int>()
        val prog = ArrayList<List<String>>()

        for (raw in source.lines()) {
            val line = raw.substringBefore(';').trim()
            if (line.isEmpty()) continue
            if (line.endsWith(":")) {
                labels[line.dropLast(1).trim().lowercase()] = prog.size
            } else {
                prog.add(line.replace(",", " ").trim().split(Regex("\\s+")))
            }
        }

        val reg = LongArray(8)
        val regRe = Regex("(?i)r([0-7])")

        fun regIndex(s: String): Int {
            val m = regRe.matchEntire(s) ?: throw IllegalArgumentException("Thanh ghi sai: $s")
            return m.groupValues[1].toInt()
        }

        fun value(s: String): Long =
            if (regRe.matches(s)) reg[regIndex(s)]
            else s.toLongOrNull() ?: throw IllegalArgumentException("Giá trị sai: $s")

        fun target(s: String): Int =
            labels[s.lowercase()] ?: throw IllegalArgumentException("Không có nhãn: $s")

        var pc = 0
        var steps = 0
        try {
            while (pc < prog.size) {
                if (++steps > maxSteps) {
                    out.append("Dừng: vượt quá $maxSteps bước\n")
                    break
                }
                val ins = prog[pc]
                val op = ins[0].uppercase()
                pc++
                when (op) {
                    "MOV" -> reg[regIndex(ins[1])] = value(ins[2])
                    "ADD" -> reg[regIndex(ins[1])] += value(ins[2])
                    "SUB" -> reg[regIndex(ins[1])] -= value(ins[2])
                    "MUL" -> reg[regIndex(ins[1])] *= value(ins[2])
                    "PRINT" -> out.append(value(ins[1])).append('\n')
                    "JMP" -> pc = target(ins[1])
                    "JNZ" -> if (value(ins[1]) != 0L) pc = target(ins[2])
                    "HALT" -> return out.toString()
                    else -> throw IllegalArgumentException("Lệnh không hợp lệ: ${ins[0]}")
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            out.append("Lỗi lệnh số ${pc}: thiếu toán hạng\n")
        } catch (e: Exception) {
            out.append("Lỗi lệnh số ${pc}: ${e.message}\n")
        }
        return out.toString()
    }
}
