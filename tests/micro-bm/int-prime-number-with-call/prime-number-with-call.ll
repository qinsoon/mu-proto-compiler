; ModuleID = 'prime-number-with-call.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.7.4"

define i32 @isPrime(i64 %a) nounwind uwtable readnone ssp {
  br label %1

; <label>:1                                       ; preds = %6, %0
  %i.0 = phi i64 [ 2, %0 ], [ %7, %6 ]
  %2 = icmp slt i64 %i.0, %a
  br i1 %2, label %3, label %8

; <label>:3                                       ; preds = %1
  %4 = srem i64 %a, %i.0
  %5 = icmp eq i64 %4, 0
  br i1 %5, label %8, label %6

; <label>:6                                       ; preds = %3
  %7 = add nsw i64 %i.0, 1
  br label %1

; <label>:8                                       ; preds = %1, %3
  %.0 = phi i32 [ 0, %3 ], [ 1, %1 ]
  ret i32 %.0
}

define i32 @main() nounwind uwtable readnone ssp {
  br label %.preheader

.preheader:                                       ; preds = %isPrime.exit, %0
  %i.02 = phi i64 [ 0, %0 ], [ %9, %isPrime.exit ]
  %sum.01 = phi i32 [ 0, %0 ], [ %8, %isPrime.exit ]
  br label %1

; <label>:1                                       ; preds = %.preheader, %6
  %i.0.i = phi i64 [ %7, %6 ], [ 2, %.preheader ]
  %2 = icmp slt i64 %i.0.i, %i.02
  br i1 %2, label %3, label %isPrime.exit

; <label>:3                                       ; preds = %1
  %4 = srem i64 %i.02, %i.0.i
  %5 = icmp eq i64 %4, 0
  br i1 %5, label %isPrime.exit, label %6

; <label>:6                                       ; preds = %3
  %7 = add nsw i64 %i.0.i, 1
  br label %1

isPrime.exit:                                     ; preds = %1, %3
  %.0.i = phi i32 [ 0, %3 ], [ 1, %1 ]
  %8 = add nsw i32 %.0.i, %sum.01
  %9 = add nsw i64 %i.02, 1
  %exitcond = icmp eq i64 %9, 1001
  br i1 %exitcond, label %10, label %.preheader

; <label>:10                                      ; preds = %isPrime.exit
  ret i32 %8
}
