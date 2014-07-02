; ModuleID = 'prime-number-with-call-isPrime.c'
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
