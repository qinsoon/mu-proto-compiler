; ModuleID = 'div-intensive.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.7.4"

define i32 @main() nounwind uwtable readnone ssp {
  br label %1

; <label>:1                                       ; preds = %0, %1
  %n.02 = phi i32 [ 0, %0 ], [ %2, %1 ]
  %sum.01 = phi double [ 0.000000e+00, %0 ], [ %5, %1 ]
  %2 = add nsw i32 %n.02, 1
  %3 = sitofp i32 %2 to double
  %4 = fdiv double 1.000000e+00, %3
  %5 = fadd double %sum.01, %4
  %6 = fcmp olt double %5, 2.000000e+01
  br i1 %6, label %1, label %7

; <label>:7                                       ; preds = %1
  %8 = fptosi double %5 to i32
  ret i32 %8
}
