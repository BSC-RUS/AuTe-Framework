/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.at.executor.helper;

import org.junit.Test;
import ru.bsc.test.at.executor.exception.ComparisonException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class ServiceRequestsComparatorHelperTest {

    private ServiceRequestsComparatorHelper serviceRequestsComparatorHelper = new ServiceRequestsComparatorHelper();

    @Test
    public void compareWSRequestStringTest() {
        invokeCompareWSRequest("aaa*ignore*bbb","aaa3434534534534534553bbb", null);
        invokeCompareWSRequest("aaa*ignore*bb","aaa3434534534534534553bbb", null);

        Throwable thrown = catchThrowable(() -> {
            invokeCompareWSRequest("aaa*ignore*b1b", "aaa3434534534534534553bbb", null);
        });
        assertThat(thrown).isInstanceOf(ComparisonException.class);



        thrown = catchThrowable(() -> {
            invokeCompareWSRequest("aaa", "aaa3434534534534534553bbb", null);
        });
        assertThat(thrown).isInstanceOf(ComparisonException.class);
    }

    @Test
    public void compareWSRequestXMLTest(){
        invokeCompareWSRequest("<a><b>3</b></a>","<a><b>3</b></a>", null);

        Throwable thrown = catchThrowable(() -> {
            invokeCompareWSRequest("<a><b>3</b></a>", "<a><c>4</c></a>", null);
        });
        assertThat(thrown).isInstanceOf(ComparisonException.class);
    }

    @Test
    public void compareWSRequestRealTest(){
        String s1 = "*ignore*\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"*ignore*gp-applications.pdf\"\n" +
                "Content-Type: application/pdf\n" +
                "Content-Length: 9234\n" +
                "\n" +
                "%PDF-1.4\n" +
                "*ignore*";
        String s2 = "--f7281597-73a2-4273-8615-2e148385203e\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"IBS_5661157057632456548245034gp-applications.pdf\"\n" +
                "Content-Type: application/pdf\n" +
                "Content-Length: 9234\n" +
                "\n" +
                "%PDF-1.4\n" +
                "%����\n" +
                "6 0 obj\n" +
                "<</Length 3682/Filter/FlateDecode>>stream\n" +
                "x��ZKo\u001C�\u0011��W�%\u0000\u0005,G��\u001E���6\u0012䒘�0\b\f:1\u0012P��\u0010���O�\u0017\u0018�P�\u00040��\"%'\b�\u0000\u001A.��͝��̬HJ!�MU?f_��Z\\+\u0014�n��GW}U�\u007F^������w��\u0017+\u001F���j�&����P���#J��ݕ۟0�l�����[�\u007F�kǗP�yw�&��,��cm����6a\u0014o�'|�B-\u0007\u007F��+T���O��/WV\u001FeGY'��7�v\u0002�Rf��c�;)�\u0012����Sx��5 ~�[\u0018��B\\\u000FϵI�\u0004��\u00144�D��B@��2\u0014�k\u0004jS\u0014aseu7\u001B\fx~\u038B�8&�q�'��yʋ\"N\u000B���(\uF4CD���[^���\u0014$;��T��4͎IVvx\t'\uD87E\uDCBC�ڸE��\u05CD�ON\u0004��9?\u0012E��<먃�\t�QS\u0000bR\u0002;���\u0012L\u0000�\u001E\u000B]\u0005��4p$�\"3�\u0016\u0002P��~�fݸ��%\u0002\u001D���(�q�\u0007���.��3!Z$\u0011g<��-\u000F@�X]$H\u001A��\u0018J\u0011\u0013�\n" +
                ".5�u@)�$;��\u001E���?�l}�\u0019?���g�\u001Fi��\u000BC���?�\u0019h�Pf1�bQHɑ�\"\u0003��\u0001���U\u0017���\u0010�<��\u0016�%O��߉[d�Xd7{�;h��\u001F�Z�k\u0011�~D\u001E��\u0012�^\u0014�4j���\u0002P;�Q\u00162\n" +
                ".�~�ܻ�@}˙3C�\u0018��Qe�8\u0015���=\u0012\u0004�K���\u0016�n�\\���\n" +
                "\u001F\u00101��\u000F�\u0013�\u0002�#�\u001E?|�|wgo�<��}���y�'�{ۍ(��\u0007P3(\u0003�A��\u000E#Ǖ(O��$��;��\u0018\u0011Ǉu\f+�\u0003���s\u0001�J^<��~��������ދ�����v�\u0001螐�\u001D�r����M����\u0006��\u0002�G��`�����[� C��gUY\u000B�v��-�R��j~d�Yά�6��NZ't��V�� .�;d�}p���H���(��K�~�d���\u007FZ-)\u001CQg����\u001E��^�Z/���s��k/�����\u001E�����\u001E�\u0003��ZѬ��X#��j�>\u0004G�~�#=��gį�[\u001Dd�\u0002���@(௳�\u000E�X�Q�\u0003[�����~�ǭ;�/p]���{A�7���=�'q�\\���礃���iN�\u001D�Y��\u0017p\u0016BUru.\u0012\f;`��[�-&=��\u0017�x�����-\u001F���}��g[^DB7\u0002��g��{ԽO��rt���\n" +
                "˝�\u001D�=_\u001B��\u000B��ҤZ!��qR�\u0019��\u000E*\u0013C]?�a�8� ���oX�e����\u001B\\\t!���!*�8�%1�?Z�\u000E�\u0013\u0012�\n" +
                "\u0017��\u001F��2NDO�ë�\u0018A\n" +
                "=�E�A>MG�@�\u0014CH�Zҍ[˃\t\u001E�٬�\b\u0004����cb\u000F�B�<$E2<�9�f`�s�uH�9?��\n" +
                "��$� }�\u0017�m�?����sQ$J2\u0001\u0011�$�\u0003�X��\u0001��B\f�Q\u0002s�4q�\n" +
                "��7O\u0003j`����́\bu�5\t��ҋSx\u0015�BR\u00071\u0014R�T5�\u0010��W��0\u0016*�̱�@+:�T���~~щ'\\u\b�O��(1zI'��\u0018��5�I�YO3]ۡn\b\t~�e֒̚�\u000F>\n" +
                "��s�7�h\u001A\u0007��n2\u00023���\u0014�hE���V�Պ1�@\u000F�peĊxu�\f�\u001B\u0016I�\u0011���\u000B\u0005K�� \u0002m�0�][�$�=c`F-�,\u0007Q�߇'�\u001C�;�r�n���\u0004yQӱr�xV߸ު�\u0006��q\u000Fx;\u0007\u0098^B\"j�s>h4�\u0013�\u0018�EQh(]\u0010a��\f*R1�\u0014\u0017��\"\u001FG�\u0013\u0000?,\u0006\u0019D�\u0014�6\u0006d��q�\u0015���Ua$��\bH�,\u0000�Ks�ڣQd�\u0002u\"�Ik�J��O��K��\u0000��{\"�X]Z#��L�x��P�����\u0003g�N�B�ʈ���o\u0004\n" +
                "�E0�.չ��Y$�1+�ȁt\u0004\u001D���8ģ\u001Ej����r�\u0006T�K\u001D\u001A���3�1k��5���P�\t�g\u001CM\u000Fe�P�.~���p�\u0018��Ĝb�@�o�J�\u0001wa�+\u0017ks\u0015�x����\u0015�\u0013'W��\u0002:=\u001C�zC�d\n" +
                "��&=6PıF\u0002�¥!\u000B �a��<(��#�\u000B\u00139��H���#A�[Q]�Ae\u0015��N�%P�\u007F�OJ�\u0005(�1�$P��6V��<//@��D�#���v���c�\u0000ǡM�\b\u0001TWr\u0010L��\b�?\u001A� ���\u001D_��rD��Q'\u0019)��\b�!H�\bJ���ビd�O�r�+ڴ�\u0015��(i�a\n" +
                ":�r\u0010�\"{�*\u001AT+\u0012\u00160�8:�P��\f�*rsl0�Rd�'�p�\u0000�\n" +
                "��\u0007-/8�\u0001����\u0019h�٦�u(��PkP�\fDQ�L�1Y/���XS�^�tfx$Z\u0004Xq\u000F3\u000B\u058B����(��4��\f�[[hؚ_���\\��\u0007��%�B�QM��\u000E{�\u001Fã1kJ��2g\u0001\f-\u007F3I���X5�\u000B\u001At<v�5\"z�x\u0003�Q�\"�$�&�\u0019JkpL�紕V��U�\n" +
                "�+��/&H\u001A'\b�\u0005�rd�?\b�%)�\u0000�z���d�R^�>\u0014\u001F�\u00002'QM\u0017�;� u\u07B3��m{M9��\"C�mߩh�7��!́ ��n|\u0005\\��� 3��\u0012�\u00040���Bh-�!fFQ��.{X�Vy�\u001Fc\u000E\u0005����R~�\u0018\u001Em�&S��.�C��·��\u0000W\u0010���jB\u000B�(.�\u0001�Kյ\u001F,�Oa\u0016���<\u00076\u007F��\u0011�D��\u0012\b*\u001DD\u007F8�w.\u0000%,�<.ވתM\u007F��\u0018�)��'�p�%��.�?\b������\u0001�1�ٶ�Ў��5g\u0006\u000F�����\"�cƗƦ�<5,l�;ȣ�4�b\u0002~�\\\u0015�Qzٺ\t�`��F�\uDAB7\uDEBC�!�y7�<\u0005}c��\u000F.\u000Bl�����H̀�$I\fv!\u0007��&�\u0012\u0015ހF`\n" +
                "̳>�\u0018���6\u0006g�W12\u000B\b\u0003粪Q�4!�����F���A(�H]��\u0006\u0016s\u0016���r�\bb�\u0013\u0018��*�^�\u0015\u0017\u001F����\u00023� DU\u001C\u0004\u0013�I\u001Ew��Qd]\u0001�\u0007\u0016��\f^eB\\8\u0012�r\u0003�Ϛ\u0002,����>\u0004��F\uFADA\bbVrՕ$���z})o\u0012���&���\b�t\u000B\u0010�ps1�ӚX[+\u0001��(�\u000E�p\u000E��� �\u0019O.N7n��\u001Ceݬsq�T\u001D\u0007vc�\u0019V\u001F!Yd\u0007�m��Il����\u0006#\u0005?\u001CM\u001E��'�k�VU\u0004!�S�\t���\u0007��!��O\u0017�F�@�B�\\fSG���\u001EZՀ��!P�WB5{'��\"N�@�EOv[�H\"��zمz+���\n" +
                "\u001B\u0003d��5ߐ��[��=��a�c��5\"��hiZ�:�l�jU췙p7��嫗�\b��Xbx*��L�ѹ#{���G^��lv\u0010j\t�A(\u0011\t��\n" +
                "������l9L�cF��P4���E�\u001D�A?��?����IK���\\S\u001FЍ!\u0588\u0016�<z�Fc`\u0001+�\u0006�\u000B)����A�\u0007�9�@���\u0011ڵ���\u00118�\u0015�f\u007F�\u0002=sX�m(�:\u0018���\u0018W�j�.s�\u0006s�\u0007a��V\u0011�<a�����ӷ?q�?ݩ�X5̸b0�U�'\n" +
                "-ǝxКg�I��\u0014�>�\u001Dxf\u0014]�}�t-��\u001D�m\u0011L�z��,t�c��Щs#�̎,��63\u0004eF�\u0011��\u0007�����\"��k\u0005��Y\u000B=˻��ai\"2��\u0002�;�~�]?����,\u000E\u001C��\u000Ft\u001E�\n" +
                "<V-2}\u001D��B��]�4��-3�\u001E6����0�\u001E6��y=�\u001E6͋��dz�\\�� �^݁��ď֞�\u0002�M��/\u000B~\u0014_L1~�\u0015\u0003�-3�\u001E�b��-3�\u001E���L\u000F��E[f2=,�\u0018��\u001F,��C��}){��\u001C�[4\u001C�u�\\\uEC9A��7l�-��w\u001B\f�]z.Z�پ�mU��qS��<s\n" +
                "�f�M�Du��͎��[���츐\u0005\u0019�1�s�\u000F��\u0012\u001Ap!�*�mU��Q��u+u�dv4jp+5�dvܬ\u07BAU�f��T�ȋ\u0017�v`�|��av\u001BJ�$���\u0017����\u000Fg>\u000B\u0005\u00147�\u0001\u0005'[f2=��BM@�*��\u0007\u00150�\t(TE��i^�e&�Ô�\u0017C\u001D�\u001Au�k�0�\u001E6�0���%ӃF�\u001AT.�\u001E6͋��dz��3�'o�D\u0002Ͻ�bk\n" +
                "�t��<.�Mw�;���>~���~~<��t���u�A\u0002��\u0002\u0012\b�@�ګ>��M\u0010Pt�D#�\u0013��Y�a�\u001F�\u0012l3[%���W���\u0018��p�l�A�9��i���Z�-�\u001B�{Po�&\u0005ɺ؟��x�Ue�U����$>ߛ��g�9\u0012`�\u007F\u0002�yse�,�H\u0002d�\b��n(��\u001Expk�;\u0014�BeR(�>\u0000 ǝ��;gA`^\u0000(X��~0����(��Э�PgO7V��a����\f([\u0006��M#�T�\u000F\\\u000B�\n" +
                "!�Y�$7n]'���Z\u0019�Y�J֨D\u0010Y�NHs�0n\u0018�F�\ta\u0018�,\u0016�H\u001A]��j�Nm��&��ܠx\u001D?f\u0019ū����\u0014�3�\u0017\u0003^[���\u0002o\u001E\u0005zwn��\u0006��\"ea`�\u0007&��f���l�M��\u0003�8�\t\n" +
                "endstream\n" +
                "endobj\n" +
                "1 0 obj\n" +
                "<</Group<</Type/Group/CS/DeviceRGB/S/Transparency>>/Parent 7 0 R/Contents 6 0 R/Type/Page/Resources<</ProcSet [/PDF /Text /ImageB /ImageC /ImageI]/ColorSpace<</CS/DeviceRGB>>/Font<</F1 2 0 R/F2 3 0 R/F3 4 0 R/F4 5 0 R>>>>/MediaBox[0 0 600 900]>>\n" +
                "endobj\n" +
                "8 0 obj\n" +
                "[1 0 R/XYZ 0 912 0]\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<</BaseFont/Helvetica/Type/Font/Encoding/WinAnsiEncoding/Subtype/Type1>>\n" +
                "endobj\n" +
                "9 0 obj\n" +
                "<</FontBBox[-558 -306 2000 1025]/CapHeight 699/Type/FontDescriptor/StemV 80/Descent -215/Flags 262176/FontName/TimesNewRomanPS-BoldMT/Ascent 677/ItalicAngle 0>>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<</LastChar 255/BaseFont/TimesNewRomanPS-BoldMT/Type/Font/Encoding<</Type/Encoding/Differences[32/space 46/period 48/zero/one/two/three/four/five 55/seven/eight/nine 185/afii61352 196/afii10021 206/afii10032 224/afii10065 226/afii10067/afii10068/afii10069/afii10070 232/afii10074 234/afii10076/afii10077 237/afii10079/afii10080 240/afii10082/afii10083 243/afii10085 251/afii10093 255/afii10097]>>/Subtype/TrueType/FontDescriptor 9 0 R/Widths[250 0 0 0 0 0 0 0 0 0 0 0 0 0 250 0 500 500 500 500 500 500 0 500 500 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1004 0 0 0 0 0 0 0 0 0 0 687 0 0 0 0 0 0 0 0 0 777 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 500 0 540 454 505 443 0 0 576 0 576 561 0 576 500 0 556 443 0 500 0 0 0 0 0 0 0 780 0 0 0 541]/FirstChar 32>>\n" +
                "endobj\n" +
                "10 0 obj\n" +
                "<</FontBBox[-568 -306 2000 1006]/CapHeight 699/Type/FontDescriptor/StemV 80/Descent -215/Flags 32/FontName/TimesNewRomanPSMT/Ascent 693/ItalicAngle 0>>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<</LastChar 255/BaseFont/TimesNewRomanPSMT/Type/Font/Encoding<</Type/Encoding/Differences[32/space 37/percent 40/parenleft/parenright 44/comma/hyphen/period/slash/zero/one/two/three/four/five/six/seven/eight/nine/colon/semicolon 67/C 69/E 73/I 76/L 83/S/T 95/underscore 97/a 101/e 105/i 108/l/m 118/v 149/bullet/endash 171/guillemotleft 185/afii61352 187/guillemotright 192/afii10017/afii10018/afii10019/afii10020/afii10021/afii10022 200/afii10026/afii10027/afii10028/afii10029/afii10030/afii10031/afii10032/afii10033/afii10034/afii10035/afii10036/afii10037/afii10038/afii10039/afii10040 216/afii10042 219/afii10045/afii10046 223/afii10049/afii10065/afii10066/afii10067/afii10068/afii10069/afii10070/afii10072/afii10073/afii10074/afii10075/afii10076/afii10077/afii10078/afii10079/afii10080/afii10081/afii10082/afii10083/afii10084/afii10085/afii10086/afii10087/afii10088/afii10089/afii10090/afii10091/afii10092/afii10093/afii10094/afii10095/afii10096/afii10097]>>/Subtype/TrueType/FontDescriptor 10 0 R/Widths[250 0 0 0 0 833 0 0 333 333 0 0 250 333 250 277 500 500 500 500 500 500 500 500 500 500 277 277 0 0 0 0 0 0 0 666 0 610 0 0 0 333 0 0 610 0 0 0 0 0 0 556 610 0 0 0 0 0 0 0 0 0 0 500 0 443 0 0 0 443 0 0 0 277 0 0 277 777 0 0 0 0 0 0 0 0 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 350 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 500 0 0 0 0 0 0 0 0 0 0 0 0 0 954 0 500 0 0 0 0 722 574 666 578 682 610 0 0 722 722 666 678 889 722 722 722 556 666 610 708 790 722 722 0 1008 0 0 872 574 0 0 666 443 508 472 410 508 443 690 395 535 535 485 499 632 535 500 535 500 443 437 500 647 500 535 502 770 770 517 671 456 429 747 459]/FirstChar 32>>\n" +
                "endobj\n" +
                "11 0 obj\n" +
                "<</FontBBox[-497 -306 1120 1023]/CapHeight 699/Type/FontDescriptor/StemV 80/Descent -215/Flags 96/FontName/TimesNewRomanPS-ItalicMT/Ascent 694/ItalicAngle -14.33>>\n" +
                "endobj\n" +
                "5 0 obj\n" +
                "<</LastChar 254/BaseFont/TimesNewRomanPS-ItalicMT/Type/Font/Encoding<</Type/Encoding/Differences[32/space 40/parenleft/parenright 44/comma 46/period 95/underscore 193/afii10018/afii10019 200/afii10026 206/afii10032 212/afii10038 224/afii10065 228/afii10069 230/afii10072 232/afii10074 234/afii10076/afii10077 237/afii10079/afii10080/afii10081/afii10082/afii10083/afii10084/afii10085 247/afii10089 252/afii10094 254/afii10096]>>/Subtype/TrueType/FontDescriptor 11 0 R/Widths[250 0 0 0 0 0 0 0 333 333 0 0 250 0 250 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 589 610 0 0 0 0 0 722 0 0 0 0 0 722 0 0 0 0 0 804 0 0 0 0 0 0 0 0 0 0 0 500 0 0 0 505 0 930 0 500 0 467 436 0 496 500 500 500 443 722 443 0 0 0 477 0 0 0 0 455 0 705]/FirstChar 32>>\n" +
                "endobj\n" +
                "7 0 obj\n" +
                "<</ITXT(2.1.7)/Type/Pages/Count 1/Kids[1 0 R]>>\n" +
                "endobj\n" +
                "12 0 obj\n" +
                "<</Names[(JR_PAGE_ANCHOR_0_1) 8 0 R]>>\n" +
                "endobj\n" +
                "13 0 obj\n" +
                "<</Dests 12 0 R>>\n" +
                "endobj\n" +
                "14 0 obj\n" +
                "<</Names 13 0 R/Type/Catalog/ViewerPreferences<</PrintScaling/AppDefault>>/Pages 7 0 R>>\n" +
                "endobj\n" +
                "15 0 obj\n" +
                "<</Creator(JasperReports \\(RC_DepositAgreement\\))/Producer(iText 2.1.7 by 1T3XT)/ModDate(D:20141208165959+03'00')/CreationDate(D:20141208165959+03'00')>>\n" +
                "endobj\n" +
                "xref\n" +
                "0 16\n" +
                "0000000000 65535 f \n" +
                "0000003765 00000 n \n" +
                "0000004061 00000 n \n" +
                "0000004325 00000 n \n" +
                "0000005473 00000 n \n" +
                "0000007332 00000 n \n" +
                "0000000015 00000 n \n" +
                "0000008337 00000 n \n" +
                "0000004026 00000 n \n" +
                "0000004149 00000 n \n" +
                "0000005305 00000 n \n" +
                "0000007152 00000 n \n" +
                "0000008400 00000 n \n" +
                "0000008455 00000 n \n" +
                "0000008489 00000 n \n" +
                "0000008594 00000 n \n" +
                "trailer\n" +
                "<</Root 14 0 R/ID [<91639c7a5e10db8aad37c9a757d835d3><e3ecf36f279eca665ac80f910109e5cf>]/Info 15 0 R/Size 16>>\n" +
                "startxref\n" +
                "8764\n" +
                "%%EOF\n" +
                "\n" +
                "--f7281597-73a2-4273-8615-2e148385203e--\n";
        invokeCompareWSRequest(s1,s2, null);

    }

    @Test
    public void compareWSRequestRealNotEqualTestCOM236(){
        invokeCompareWSRequest("{\"phoneNumber\":\"8888888888\",\"text\":\"Код подтверждения: 1234 для подписания заявления на кредит и заявления на перевод страховой премии в размере 4 000,00 руб (при оформлении кредита) Хэш-код по документу: 454bfa984d224a3b447c538957b42e7e8f36e8165d8d29aad2dcc8f2121d7333\",\"documentHash\":\"454bfa984d224a3b447c538957b42e7e8f36e8165d8d29aad2dcc8f2121d7333\",\"sendDate\":\"2018-05-15*ignore*\",\"signatureCode\":\"1234\",\"statusHistory\":[{\"actorLogin\":\"skryazhev\"}]}",
                               "{\"phoneNumber\":\"8888888888\",\"text\":\"Код подтверждения: 1234 для подписания заявления на кредит и заявления на перевод страховой премии в размере 4 000,00 руб (при оформлении кредита) Хэш-код по документу: 454bfa984d224a3b447c538957b42e7e8f36e8165d8d29aad2dcc8f2121d7333\",\"documentHash\":\"454bfa984d224a3b447c538957b42e7e8f36e8165d8d29aad2dcc8f2121d7333\",\"sendDate\":\"2018-05-15T10:17:03.813Z\",\"signatureCode\":\"1234\",\"statusHistory\":[{\"actorLogin\":\"skryazhev\"}]}",
                null);
    }

    @Test
    public void compareWSRequestRealNotEqualTest(){
        final String s1 = "some2";
        final String s2 = "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"hashString\"\n" +
                "Content-Length: 64\n" +
                "\n" +
                "454bfa984d224a3b447c538957b42e7e8f36e8165d8d29aad2dcc8f2121d7111\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"clientLastName\"\n" +
                "Content-Length: 12\n" +
                "\n" +
                "КРЯЖЕВ\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"isSigned\"\n" +
                "Content-Length: 1\n" +
                "\n" +
                "0\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"scan_contents\"; filename=\"IBS_5661162804225762545900731gp-applications.pdf\"\n" +
                "Content-Type: application/pdf\n" +
                "Content-Length: 9234\n" +
                "\n" +
                "%PDF-1.4\n" +
                "%����\n" +
                "6 0 obj\n" +
                "<</Length 3682/Filter/FlateDecode>>stream\n" +
                "x��ZKo\u001C�\u0011��W�%\u0000\u0005,G��\u001E���6\u0012䒘�0\b\f:1\u0012P��\u0010���O�\u0017\u0018�P�\u00040��\"%'\b�\u0000\u001A.��͝��̬HJ!�MU?f_��Z\\+\u0014�n��GW}U�\u007F^������w��\u0017+\u001F���j�&����P���#J��ݕ۟0�l�����[�\u007F�kǗP�yw�&��,��cm����6a\u0014o�'|�B-\u0007\u007F��+T���O��/WV\u001FeGY'��7�v\u0002�Rf��c�;)�\u0012����Sx��5 ~�[\u0018��B\\\u000FϵI�\u0004��\u00144�D��B@��2\u0014�k\u0004jS\u0014aseu7\u001B\fx~\u038B�8&�q�'��yʋ\"N\u000B���(\uF4CD���[^���\u0014$;��T��4͎IVvx\t'\uD87E\uDCBC�ڸE��\u05CD�ON\u0004��9?\u0012E��<먃�\t�QS\u0000bR\u0002;���\u0012L\u0000�\u001E\u000B]\u0005��4p$�\"3�\u0016\u0002P��~�fݸ��%\u0002\u001D���(�q�\u0007���.��3!Z$\u0011g<��-\u000F@�X]$H\u001A��\u0018J\u0011\u0013�\n" +
                ".5�u@)�$;��\u001E���?�l}�\u0019?���g�\u001Fi��\u000BC���?�\u0019h�Pf1�bQHɑ�\"\u0003��\u0001���U\u0017���\u0010�<��\u0016�%O��߉[d�Xd7{�;h��\u001F�Z�k\u0011�~D\u001E��\u0012�^\u0014�4j���\u0002P;�Q\u00162\n" +
                ".�~�ܻ�@}˙3C�\u0018��Qe�8\u0015���=\u0012\u0004�K���\u0016�n�\\���\n" +
                "\u001F\u00101��\u000F�\u0013�\u0002�#�\u001E?|�|wgo�<��}���y�'�{ۍ(��\u0007P3(\u0003�A��\u000E#Ǖ(O��$��;��\u0018\u0011Ǉu\f+�\u0003���s\u0001�J^<��~��������ދ�����v�\u0001螐�\u001D�r����M����\u0006��\u0002�G��`�����[� C��gUY\u000B�v��-�R��j~d�Yά�6��NZ't��V�� .�;d�}p���H���(��K�~�d���\u007FZ-)\u001CQg����\u001E��^�Z/���s��k/�����\u001E�����\u001E�\u0003��ZѬ��X#��j�>\u0004G�~�#=��gį�[\u001Dd�\u0002���@(௳�\u000E�X�Q�\u0003[�����~�ǭ;�/p]���{A�7���=�'q�\\���礃���iN�\u001D�Y��\u0017p\u0016BUru.\u0012\f;`��[�-&=��\u0017�x�����-\u001F���}��g[^DB7\u0002��g��{ԽO��rt���\n" +
                "˝�\u001D�=_\u001B��\u000B��ҤZ!��qR�\u0019��\u000E*\u0013C]?�a�8� ���oX�e����\u001B\\\t!���!*�8�%1�?Z�\u000E�\u0013\u0012�\n" +
                "\u0017��\u001F��2NDO�ë�\u0018A\n" +
                "=�E�A>MG�@�\u0014CH�Zҍ[˃\t\u001E�٬�\b\u0004����cb\u000F�B�<$E2<�9�f`�s�uH�9?��\n" +
                "��$� }�\u0017�m�?����sQ$J2\u0001\u0011�$�\u0003�X��\u0001��B\f�Q\u0002s�4q�\n" +
                "��7O\u0003j`����́\bu�5\t��ҋSx\u0015�BR\u00071\u0014R�T5�\u0010��W��0\u0016*�̱�@+:�T���~~щ'\\u\b�O��(1zI'��\u0018��5�I�YO3]ۡn\b\t~�e֒̚�\u000F>\n" +
                "��s�7�h\u001A\u0007��n2\u00023���\u0014�hE���V�Պ1�@\u000F�peĊxu�\f�\u001B\u0016I�\u0011���\u000B\u0005K�� \u0002m�0�][�$�=c`F-�,\u0007Q�߇'�\u001C�;�r�n���\u0004yQӱr�xV߸ު�\u0006��q\u000Fx;\u0007\u0098^B\"j�s>h4�\u0013�\u0018�EQh(]\u0010a��\f*R1�\u0014\u0017��\"\u001FG�\u0013\u0000?,\u0006\u0019D�\u0014�6\u0006d��q�\u0015���Ua$��\bH�,\u0000�Ks�ڣQd�\u0002u\"�Ik�J��O��K��\u0000��{\"�X]Z#��L�x��P�����\u0003g�N�B�ʈ���o\u0004\n" +
                "�E0�.չ��Y$�1+�ȁt\u0004\u001D���8ģ\u001Ej����r�\u0006T�K\u001D\u001A���3�1k��5���P�\t�g\u001CM\u000Fe�P�.~���p�\u0018��Ĝb�@�o�J�\u0001wa�+\u0017ks\u0015�x����\u0015�\u0013'W��\u0002:=\u001C�zC�d\n" +
                "��&=6PıF\u0002�¥!\u000B �a��<(��#�\u000B\u00139��H���#A�[Q]�Ae\u0015��N�%P�\u007F�OJ�\u0005(�1�$P��6V��<//@��D�#���v���c�\u0000ǡM�\b\u0001TWr\u0010L��\b�?\u001A� ���\u001D_��rD��Q'\u0019)��\b�!H�\bJ���ビd�O�r�+ڴ�\u0015��(i�a\n" +
                ":�r\u0010�\"{�*\u001AT+\u0012\u00160�8:�P��\f�*rsl0�Rd�'�p�\u0000�\n" +
                "��\u0007-/8�\u0001����\u0019h�٦�u(��PkP�\fDQ�L�1Y/���XS�^�tfx$Z\u0004Xq\u000F3\u000B\u058B����(��4��\f�[[hؚ_���\\��\u0007��%�B�QM��\u000E{�\u001Fã1kJ��2g\u0001\f-\u007F3I���X5�\u000B\u001At<v�5\"z�x\u0003�Q�\"�$�&�\u0019JkpL�紕V��U�\n" +
                "�+��/&H\u001A'\b�\u0005�rd�?\b�%)�\u0000�z���d�R^�>\u0014\u001F�\u00002'QM\u0017�;� u\u07B3��m{M9��\"C�mߩh�7��!́ ��n|\u0005\\��� 3��\u0012�\u00040���Bh-�!fFQ��.{X�Vy�\u001Fc\u000E\u0005����R~�\u0018\u001Em�&S��.�C��·��\u0000W\u0010���jB\u000B�(.�\u0001�Kյ\u001F,�Oa\u0016���<\u00076\u007F��\u0011�D��\u0012\b*\u001DD\u007F8�w.\u0000%,�<.ވתM\u007F��\u0018�)��'�p�%��.�?\b������\u0001�1�ٶ�Ў��5g\u0006\u000F�����\"�cƗƦ�<5,l�;ȣ�4�b\u0002~�\\\u0015�Qzٺ\t�`��F�\uDAB7\uDEBC�!�y7�<\u0005}c��\u000F.\u000Bl�����H̀�$I\fv!\u0007��&�\u0012\u0015ހF`\n" +
                "̳>�\u0018���6\u0006g�W12\u000B\b\u0003粪Q�4!�����F���A(�H]��\u0006\u0016s\u0016���r�\bb�\u0013\u0018��*�^�\u0015\u0017\u001F����\u00023� DU\u001C\u0004\u0013�I\u001Ew��Qd]\u0001�\u0007\u0016��\f^eB\\8\u0012�r\u0003�Ϛ\u0002,����>\u0004��F\uFADA\bbVrՕ$���z})o\u0012���&���\b�t\u000B\u0010�ps1�ӚX[+\u0001��(�\u000E�p\u000E��� �\u0019O.N7n��\u001Ceݬsq�T\u001D\u0007vc�\u0019V\u001F!Yd\u0007�m��Il����\u0006#\u0005?\u001CM\u001E��'�k�VU\u0004!�S�\t���\u0007��!��O\u0017�F�@�B�\\fSG���\u001EZՀ��!P�WB5{'��\"N�@�EOv[�H\"��zمz+���\n" +
                "\u001B\u0003d��5ߐ��[��=��a�c��5\"��hiZ�:�l�jU췙p7��嫗�\b��Xbx*��L�ѹ#{���G^��lv\u0010j\t�A(\u0011\t��\n" +
                "������l9L�cF��P4���E�\u001D�A?��?����IK���\\S\u001FЍ!\u0588\u0016�<z�Fc`\u0001+�\u0006�\u000B)����A�\u0007�9�@���\u0011ڵ���\u00118�\u0015�f\u007F�\u0002=sX�m(�:\u0018���\u0018W�j�.s�\u0006s�\u0007a��V\u0011�<a�����ӷ?q�?ݩ�X5̸b0�U�'\n" +
                "-ǝxКg�I��\u0014�>�\u001Dxf\u0014]�}�t-��\u001D�m\u0011L�z��,t�c��Щs#�̎,��63\u0004eF�\u0011��\u0007�����\"��k\u0005��Y\u000B=˻��ai\"2��\u0002�;�~�]?����,\u000E\u001C��\u000Ft\u001E�\n" +
                "<V-2}\u001D��B��]�4��-3�\u001E6����0�\u001E6��y=�\u001E6͋��dz�\\�� �^݁��ď֞�\u0002�M��/\u000B~\u0014_L1~�\u0015\u0003�-3�\u001E�b��-3�\u001E���L\u000F��E[f2=,�\u0018��\u001F,��C��}){��\u001C�[4\u001C�u�\\\uEC9A��7l�-��w\u001B\f�]z.Z�پ�mU��qS��<s\n" +
                "�f�M�Du��͎��[���츐\u0005\u0019�1�s�\u000F��\u0012\u001Ap!�*�mU��Q��u+u�dv4jp+5�dvܬ\u07BAU�f��T�ȋ\u0017�v`�|��av\u001BJ�$���\u0017����\u000Fg>\u000B\u0005\u00147�\u0001\u0005'[f2=��BM@�*��\u0007\u00150�\t(TE��i^�e&�Ô�\u0017C\u001D�\u001Au�k�0�\u001E6�0���%ӃF�\u001AT.�\u001E6͋��dz��3�'o�D\u0002Ͻ�bk\n" +
                "�t��<.�Mw�;���>~���~~<��t���u�A\u0002��\u0002\u0012\b�@�ګ>��M\u0010Pt�D#�\u0013��Y�a�\u001F�\u0012l3[%���W���\u0018��p�l�A�9��i���Z�-�\u001B�{Po�&\u0005ɺ؟��x�Ue�U����$>ߛ��g�9\u0012`�\u007F\u0002�yse�,�H\u0002d�\b��n(��\u001Expk�;\u0014�BeR(�>\u0000 ǝ��;gA`^\u0000(X��~0����(��Э�PgO7V��a����\f([\u0006��M#�T�\u000F\\\u000B�\n" +
                "!�Y�$7n]'���Z\u0019�Y�J֨D\u0010Y�NHs�0n\u0018�F�\ta\u0018�,\u0016�H\u001A]��j�Nm��&��ܠx\u001D?f\u0019ū����\u0014�3�\u0017\u0003^[���\u0002o\u001E\u0005zwn��\u0006��\"ea`�\u0007&��f���l�M��\u0003�8�\t\n" +
                "endstream\n" +
                "endobj\n" +
                "1 0 obj\n" +
                "<</Group<</Type/Group/CS/DeviceRGB/S/Transparency>>/Parent 7 0 R/Contents 6 0 R/Type/Page/Resources<</ProcSet [/PDF /Text /ImageB /ImageC /ImageI]/ColorSpace<</CS/DeviceRGB>>/Font<</F1 2 0 R/F2 3 0 R/F3 4 0 R/F4 5 0 R>>>>/MediaBox[0 0 600 900]>>\n" +
                "endobj\n" +
                "8 0 obj\n" +
                "[1 0 R/XYZ 0 912 0]\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<</BaseFont/Helvetica/Type/Font/Encoding/WinAnsiEncoding/Subtype/Type1>>\n" +
                "endobj\n" +
                "9 0 obj\n" +
                "<</FontBBox[-558 -306 2000 1025]/CapHeight 699/Type/FontDescriptor/StemV 80/Descent -215/Flags 262176/FontName/TimesNewRomanPS-BoldMT/Ascent 677/ItalicAngle 0>>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<</LastChar 255/BaseFont/TimesNewRomanPS-BoldMT/Type/Font/Encoding<</Type/Encoding/Differences[32/space 46/period 48/zero/one/two/three/four/five 55/seven/eight/nine 185/afii61352 196/afii10021 206/afii10032 224/afii10065 226/afii10067/afii10068/afii10069/afii10070 232/afii10074 234/afii10076/afii10077 237/afii10079/afii10080 240/afii10082/afii10083 243/afii10085 251/afii10093 255/afii10097]>>/Subtype/TrueType/FontDescriptor 9 0 R/Widths[250 0 0 0 0 0 0 0 0 0 0 0 0 0 250 0 500 500 500 500 500 500 0 500 500 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1004 0 0 0 0 0 0 0 0 0 0 687 0 0 0 0 0 0 0 0 0 777 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 500 0 540 454 505 443 0 0 576 0 576 561 0 576 500 0 556 443 0 500 0 0 0 0 0 0 0 780 0 0 0 541]/FirstChar 32>>\n" +
                "endobj\n" +
                "10 0 obj\n" +
                "<</FontBBox[-568 -306 2000 1006]/CapHeight 699/Type/FontDescriptor/StemV 80/Descent -215/Flags 32/FontName/TimesNewRomanPSMT/Ascent 693/ItalicAngle 0>>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<</LastChar 255/BaseFont/TimesNewRomanPSMT/Type/Font/Encoding<</Type/Encoding/Differences[32/space 37/percent 40/parenleft/parenright 44/comma/hyphen/period/slash/zero/one/two/three/four/five/six/seven/eight/nine/colon/semicolon 67/C 69/E 73/I 76/L 83/S/T 95/underscore 97/a 101/e 105/i 108/l/m 118/v 149/bullet/endash 171/guillemotleft 185/afii61352 187/guillemotright 192/afii10017/afii10018/afii10019/afii10020/afii10021/afii10022 200/afii10026/afii10027/afii10028/afii10029/afii10030/afii10031/afii10032/afii10033/afii10034/afii10035/afii10036/afii10037/afii10038/afii10039/afii10040 216/afii10042 219/afii10045/afii10046 223/afii10049/afii10065/afii10066/afii10067/afii10068/afii10069/afii10070/afii10072/afii10073/afii10074/afii10075/afii10076/afii10077/afii10078/afii10079/afii10080/afii10081/afii10082/afii10083/afii10084/afii10085/afii10086/afii10087/afii10088/afii10089/afii10090/afii10091/afii10092/afii10093/afii10094/afii10095/afii10096/afii10097]>>/Subtype/TrueType/FontDescriptor 10 0 R/Widths[250 0 0 0 0 833 0 0 333 333 0 0 250 333 250 277 500 500 500 500 500 500 500 500 500 500 277 277 0 0 0 0 0 0 0 666 0 610 0 0 0 333 0 0 610 0 0 0 0 0 0 556 610 0 0 0 0 0 0 0 0 0 0 500 0 443 0 0 0 443 0 0 0 277 0 0 277 777 0 0 0 0 0 0 0 0 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 350 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 500 0 0 0 0 0 0 0 0 0 0 0 0 0 954 0 500 0 0 0 0 722 574 666 578 682 610 0 0 722 722 666 678 889 722 722 722 556 666 610 708 790 722 722 0 1008 0 0 872 574 0 0 666 443 508 472 410 508 443 690 395 535 535 485 499 632 535 500 535 500 443 437 500 647 500 535 502 770 770 517 671 456 429 747 459]/FirstChar 32>>\n" +
                "endobj\n" +
                "11 0 obj\n" +
                "<</FontBBox[-497 -306 1120 1023]/CapHeight 699/Type/FontDescriptor/StemV 80/Descent -215/Flags 96/FontName/TimesNewRomanPS-ItalicMT/Ascent 694/ItalicAngle -14.33>>\n" +
                "endobj\n" +
                "5 0 obj\n" +
                "<</LastChar 254/BaseFont/TimesNewRomanPS-ItalicMT/Type/Font/Encoding<</Type/Encoding/Differences[32/space 40/parenleft/parenright 44/comma 46/period 95/underscore 193/afii10018/afii10019 200/afii10026 206/afii10032 212/afii10038 224/afii10065 228/afii10069 230/afii10072 232/afii10074 234/afii10076/afii10077 237/afii10079/afii10080/afii10081/afii10082/afii10083/afii10084/afii10085 247/afii10089 252/afii10094 254/afii10096]>>/Subtype/TrueType/FontDescriptor 11 0 R/Widths[250 0 0 0 0 0 0 0 333 333 0 0 250 0 250 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 500 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 589 610 0 0 0 0 0 722 0 0 0 0 0 722 0 0 0 0 0 804 0 0 0 0 0 0 0 0 0 0 0 500 0 0 0 505 0 930 0 500 0 467 436 0 496 500 500 500 443 722 443 0 0 0 477 0 0 0 0 455 0 705]/FirstChar 32>>\n" +
                "endobj\n" +
                "7 0 obj\n" +
                "<</ITXT(2.1.7)/Type/Pages/Count 1/Kids[1 0 R]>>\n" +
                "endobj\n" +
                "12 0 obj\n" +
                "<</Names[(JR_PAGE_ANCHOR_0_1) 8 0 R]>>\n" +
                "endobj\n" +
                "13 0 obj\n" +
                "<</Dests 12 0 R>>\n" +
                "endobj\n" +
                "14 0 obj\n" +
                "<</Names 13 0 R/Type/Catalog/ViewerPreferences<</PrintScaling/AppDefault>>/Pages 7 0 R>>\n" +
                "endobj\n" +
                "15 0 obj\n" +
                "<</Creator(JasperReports \\(RC_DepositAgreement\\))/Producer(iText 2.1.7 by 1T3XT)/ModDate(D:20141208165959+03'00')/CreationDate(D:20141208165959+03'00')>>\n" +
                "endobj\n" +
                "xref\n" +
                "0 16\n" +
                "0000000000 65535 f \n" +
                "0000003765 00000 n \n" +
                "0000004061 00000 n \n" +
                "0000004325 00000 n \n" +
                "0000005473 00000 n \n" +
                "0000007332 00000 n \n" +
                "0000000015 00000 n \n" +
                "0000008337 00000 n \n" +
                "0000004026 00000 n \n" +
                "0000004149 00000 n \n" +
                "0000005305 00000 n \n" +
                "0000007152 00000 n \n" +
                "0000008400 00000 n \n" +
                "0000008455 00000 n \n" +
                "0000008489 00000 n \n" +
                "0000008594 00000 n \n" +
                "trailer\n" +
                "<</Root 14 0 R/ID [<91639c7a5e10db8aad37c9a757d835d3><e3ecf36f279eca665ac80f910109e5cf>]/Info 15 0 R/Size 16>>\n" +
                "startxref\n" +
                "8764\n" +
                "%%EOF\n" +
                "\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"appId\"\n" +
                "Content-Length: 40\n" +
                "\n" +
                "IBS-96019b67-c263-466d-8f3f-a99be4820b0d\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"clientFirstName\"\n" +
                "Content-Length: 12\n" +
                "\n" +
                "СЕРГЕЙ\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"ecm_type\"\n" +
                "Content-Length: 1\n" +
                "\n" +
                "5\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"cif_id\"\n" +
                "Content-Length: 36\n" +
                "\n" +
                "696ef08f-db8f-476d-9748-bde0fa0fa163\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"clientMiddleName\"\n" +
                "Content-Length: 18\n" +
                "\n" +
                "АНДРЕЕВИЧ\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c\n" +
                "Content-Disposition: form-data; name=\"docCreateDate\"\n" +
                "Content-Length: 24\n" +
                "\n" +
                "2018-05-08T12:55:29.762Z\n" +
                "--42b53c87-5e93-40f5-a68f-1959fd6f6c7c--\n";

        boolean f = false;
        try {
            invokeCompareWSRequest(s1, s2, null);
        }catch (ComparisonException ce){
            f = true;
        }
        assertTrue(f);

        Throwable thrown = catchThrowable(() -> {
            invokeCompareWSRequest(s1, s2, null);
        });
        assertThat(thrown).isInstanceOf(ComparisonException.class);
    }

    // TODO такой тест не пройдет, надо поправить COM-233
    public void compareWSRequestTODOTest(){
        String s1 = "<a><b>3</b></a>";
        String s2 = "<a><c>3</c></a>";

        Throwable thrown = catchThrowable(() -> {
            invokeCompareWSRequest(s1, s2, null);
        });
        assertThat(thrown).isInstanceOf(ComparisonException.class);
    }

    private void invokeCompareWSRequest(String s1, String s2, Set s) throws ComparisonException{
        try {
            Method compareWSRequest = serviceRequestsComparatorHelper.getClass().getDeclaredMethod("compareWSRequest", new Class[]{String.class, String.class, Set.class});
            compareWSRequest.setAccessible(true);
            compareWSRequest.invoke(serviceRequestsComparatorHelper, s1, s2, s);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if(e.getTargetException() instanceof ComparisonException){
                throw (ComparisonException)e.getTargetException();
            }
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
