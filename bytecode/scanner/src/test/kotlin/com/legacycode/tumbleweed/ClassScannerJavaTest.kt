package com.legacycode.tumbleweed

import com.legacycode.tumbleweed.samples.ConstantReferencedInConditional
import com.legacycode.tumbleweed.samples.ConstantReferencedInReturnStatement
import com.legacycode.tumbleweed.samples.Constants
import com.legacycode.tumbleweed.samples.IntegerConstants
import com.legacycode.tumbleweed.samples.LambdaAccessingField
import com.legacycode.tumbleweed.samples.MethodReadingField
import com.legacycode.tumbleweed.samples.MethodWritingField
import com.legacycode.tumbleweed.samples.StaticBlock
import com.legacycode.tumbleweed.samples.StringConcatenation
import com.legacycode.tumbleweed.testing.SampleClass
import org.approvaltests.Approvals
import org.junit.jupiter.api.Test

class ClassScannerJavaTest {
  @Test
  fun `01 - it can scan a class with methods reading a field`() {
    // given
    val methodReadingField = SampleClass.Java(MethodReadingField::class)

    // when
    val classStructure = ClassScanner.scan(methodReadingField.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `02 - it can scan a class with methods writing a field`() {
    // given
    val methodWritingField = SampleClass.Java(MethodWritingField::class)

    // when
    val classStructure = ClassScanner.scan(methodWritingField.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `03 - it can scan a class with a lambda function accessing a field`() {
    // given
    val methodReadingAndWritingField = SampleClass.Java(LambdaAccessingField::class)

    // when
    val classStructure = ClassScanner.scan(methodReadingAndWritingField.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `04 - it can scan a class with string concatenation`() {
    // given
    val stringConcatenation = SampleClass.Java(StringConcatenation::class)

    // when
    val classStructure = ClassScanner.scan(stringConcatenation.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `05 - it can scan a class with constants`() {
    // given
    val constants = SampleClass.Java(Constants::class)

    // when
    val classStructure = ClassScanner.scan(constants.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `06 - it can scan a class with integer constants`() {
    // given
    val integerConstants = SampleClass.Java(IntegerConstants::class)

    // when
    val classStructure = ClassScanner.scan(integerConstants.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `07 - it can scan a class with a constant referenced in a conditional`() {
    // given
    val constantReferencedInConditional = SampleClass.Java(ConstantReferencedInConditional::class)

    // when
    val classStructure = ClassScanner.scan(constantReferencedInConditional.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `08 - it can scan a class with a constant referenced in return statements`() {
    // given
    val constantReferencedInReturnStatement = SampleClass.Java(ConstantReferencedInReturnStatement::class)

    // when
    val classStructure = ClassScanner.scan(constantReferencedInReturnStatement.file)

    // then
    Approvals.verify(classStructure.printable)
  }

  @Test
  fun `09 - it can scan a class with a static block containing a function call`() {
    // given
    val staticBlock = SampleClass.Java(StaticBlock::class)

    // when
    val classStructure = ClassScanner.scan(staticBlock.file)

    // then
    Approvals.verify(classStructure.printable)
  }
}
