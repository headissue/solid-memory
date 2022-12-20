package com.headissue.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FormKeyServiceTest {

  private FormKeyService sut = new FormKeyService();

  @Test
  void isValid() {
    FormKeyService.FormKey formKey = sut.getFormKey();
    assertThat(sut.isValid(formKey.getHash(), formKey.getKey()), is(true));
  }

  @Test
  void isInValid() {
    assertThat(sut.isValid("someHash", "someKey"), is(false));
  }
}
