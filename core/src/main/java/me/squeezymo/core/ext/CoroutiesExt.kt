@file:Suppress("NOTHING_TO_INLINE", "unused")

package me.squeezymo.core.ext

import kotlinx.coroutines.flow.*

suspend inline fun <T> Flow<T>.collectTo(mutableStateFlow: MutableStateFlow<T>) {
    return collect {
        mutableStateFlow.value = it
    }
}

inline fun <T1, T2> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>
): Flow<Tuple2<T1, T2>> {
    return flow1.combine(flow2) { t1, t2 ->
        Tuple2(t1, t2)
    }
}

inline fun <T1, T2> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>
): Flow<Tuple2<T1, T2>> {
    return flow1.zip(flow2) { t1, t2 ->
        Tuple2(t1, t2)
    }
}

inline fun <T1, T2, T3> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>
): Flow<Tuple3<T1, T2, T3>> {
    return combineToTuple(flow1, flow2).combine(flow3) { (t1, t2), t3 ->
        Tuple3(t1, t2, t3)
    }
}

inline fun <T1, T2, T3> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>
): Flow<Tuple3<T1, T2, T3>> {
    return zipToTuple(flow1, flow2).zip(flow3) { (t1, t2), t3 ->
        Tuple3(t1, t2, t3)
    }
}

inline fun <T1, T2, T3, T4> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>
): Flow<Tuple4<T1, T2, T3, T4>> {
    return combineToTuple(flow1, flow2, flow3).combine(flow4) { (t1, t2, t3), t4 ->
        Tuple4(t1, t2, t3, t4)
    }
}

inline fun <T1, T2, T3, T4> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>
): Flow<Tuple4<T1, T2, T3, T4>> {
    return zipToTuple(flow1, flow2, flow3).zip(flow4) { (t1, t2, t3), t4 ->
        Tuple4(t1, t2, t3, t4)
    }
}

inline fun <T1, T2, T3, T4, T5> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>
): Flow<Tuple5<T1, T2, T3, T4, T5>> {
    return combineToTuple(flow1, flow2, flow3, flow4).combine(flow5) { (t1, t2, t3, t4), t5 ->
        Tuple5(t1, t2, t3, t4, t5)
    }
}

inline fun <T1, T2, T3, T4, T5> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>
): Flow<Tuple5<T1, T2, T3, T4, T5>> {
    return zipToTuple(flow1, flow2, flow3, flow4).zip(flow5) { (t1, t2, t3, t4), t5 ->
        Tuple5(t1, t2, t3, t4, t5)
    }
}

inline fun <T1, T2, T3, T4, T5, T6> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>
): Flow<Tuple6<T1, T2, T3, T4, T5, T6>> {
    return combineToTuple(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5
    ).combine(flow6) { (t1, t2, t3, t4, t5), t6 ->
        Tuple6(t1, t2, t3, t4, t5, t6)
    }
}

inline fun <T1, T2, T3, T4, T5, T6> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>
): Flow<Tuple6<T1, T2, T3, T4, T5, T6>> {
    return zipToTuple(flow1, flow2, flow3, flow4, flow5).zip(flow6) { (t1, t2, t3, t4, t5), t6 ->
        Tuple6(t1, t2, t3, t4, t5, t6)
    }
}

inline fun <T1, T2, T3, T4, T5, T6, T7> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>
): Flow<Tuple7<T1, T2, T3, T4, T5, T6, T7>> {
    return combineToTuple(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6
    ).combine(flow7) { (t1, t2, t3, t4, t5, t6), t7 ->
        Tuple7(t1, t2, t3, t4, t5, t6, t7)
    }
}

inline fun <T1, T2, T3, T4, T5, T6, T7> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>
): Flow<Tuple7<T1, T2, T3, T4, T5, T6, T7>> {
    return zipToTuple(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6
    ).zip(flow7) { (t1, t2, t3, t4, t5, t6), t7 ->
        Tuple7(t1, t2, t3, t4, t5, t6, t7)
    }
}


inline fun <T1, T2, T3, T4, T5, T6, T7, T8> combineToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>
): Flow<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> {
    return combineToTuple(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6,
        flow7
    ).combine(flow8) { (t1, t2, t3, t4, t5, t6, t7), t8 ->
        Tuple8(t1, t2, t3, t4, t5, t6, t7, t8)
    }
}

inline fun <T1, T2, T3, T4, T5, T6, T7, T8> zipToTuple(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>
): Flow<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> {
    return zipToTuple(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6,
        flow7
    ).zip(flow8) { (t1, t2, t3, t4, t5, t6, t7), t8 ->
        Tuple8(t1, t2, t3, t4, t5, t6, t7, t8)
    }
}
