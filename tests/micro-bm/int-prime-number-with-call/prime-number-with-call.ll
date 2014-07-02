; ModuleID = 'prime-number-with-call.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.7.4"

define i32 @main() nounwind uwtable ssp {
  br label %1

; <label>:1                                       ; preds = %1, %0
  %i.02 = phi i64 [ 2, %0 ], [ %4, %1 ]
  %sum.01 = phi i32 [ 0, %0 ], [ %3, %1 ]
  %2 = tail call i32 @isPrime(i64 %i.02) nounwind
  %3 = add nsw i32 %2, %sum.01
  %4 = add nsw i64 %i.02, 1
  %exitcond = icmp eq i64 %4, 100001
  br i1 %exitcond, label %5, label %1

; <label>:5                                       ; preds = %1
  ret i32 %3
}

declare i32 @isPrime(i64)
